package org.cotato.backend.recruit.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.cotato.backend.recruit.auth.controller.AuthController;
import org.cotato.backend.recruit.auth.dto.CustomUserDetails;
import org.cotato.backend.recruit.auth.dto.TokenResponse;
import org.cotato.backend.recruit.auth.jwt.JwtTokenProvider;
import org.cotato.backend.recruit.auth.repository.RefreshTokenRepository;
import org.cotato.backend.recruit.common.error.ErrorCode;
import org.cotato.backend.recruit.common.exception.ExceptionAnalysisLogger;
import org.cotato.backend.recruit.common.exception.GlobalException;
import org.cotato.backend.recruit.common.exception.GlobalExceptionHandler;
import org.cotato.backend.recruit.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 실제 Redis에서 Lua 원자성과 토큰 갱신 계약을 검증한다. Redis는 localhost:6379에서 실행한다. */
class RefreshTokenIntegrationTest {

	private static final String SECRET = "refresh-integration-test-secret-at-least-256-bits-long";
	private static final long REFRESH_TTL = 604800000;
	private static LettuceConnectionFactory connectionFactory;
	private static RedisTemplate<String, String> redisTemplate;

	private String userId;
	private JwtTokenProvider jwtTokenProvider;
	private RefreshTokenRepository repository;
	private AuthService service;
	private MockMvc mockMvc;

	@BeforeAll
	static void connectRedis() {
		connectionFactory = new LettuceConnectionFactory("localhost", 6379);
		connectionFactory.afterPropertiesSet();
		redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		redisTemplate.afterPropertiesSet();
		try (var connection = connectionFactory.getConnection()) {
			assertThat(connection.ping()).isEqualTo("PONG");
		}
	}

	@BeforeEach
	void setUp() {
		userId = "refresh-test-" + UUID.randomUUID();
		jwtTokenProvider = new JwtTokenProvider();
		ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", SECRET);
		ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiration", 3600000L);
		ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", REFRESH_TTL);
		ReflectionTestUtils.invokeMethod(jwtTokenProvider, "init");
		repository = new RefreshTokenRepository(redisTemplate);
		UserRepository users =
				(UserRepository)
						Proxy.newProxyInstance(
								UserRepository.class.getClassLoader(),
								new Class<?>[] {UserRepository.class},
								(proxy, method, args) -> {
									throw new AssertionError(
											"Refresh must not access the user database");
								});
		service = new AuthService(jwtTokenProvider, repository, users, new ObjectMapper());
		ReflectionTestUtils.setField(service, "refreshTokenReuseInterval", 10000L);
		// 이 테스트에서는 Google 로그인 엔드포인트를 호출하지 않는다.
		mockMvc =
				MockMvcBuilders.standaloneSetup(new AuthController(service, null))
						.setControllerAdvice(
								new GlobalExceptionHandler(
										new ExceptionAnalysisLogger(new ObjectMapper())))
						.build();
	}

	@AfterEach
	void cleanUp() {
		repository.deleteByUserId(userId);
	}

	@AfterAll
	static void disconnectRedis() {
		connectionFactory.destroy();
	}

	@Test
	void concurrentRequestsReturnOneIdenticalPair() throws Exception {
		String original = login();
		int count = 10;
		ExecutorService executor = Executors.newFixedThreadPool(count);
		CountDownLatch ready = new CountDownLatch(count);
		CountDownLatch start = new CountDownLatch(1);
		List<Future<TokenResponse>> responses = new ArrayList<>();
		try {
			for (int i = 0; i < count; i++) {
				responses.add(
						executor.submit(
								() -> {
									ready.countDown();
									assertThat(start.await(5, TimeUnit.SECONDS)).isTrue();
									return refreshThroughHttp(original);
								}));
			}
			assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
			start.countDown();
			TokenResponse first = responses.get(0).get(10, TimeUnit.SECONDS);
			for (Future<TokenResponse> response : responses) {
				assertThat(response.get(10, TimeUnit.SECONDS)).isEqualTo(first);
			}
			assertThat(first.refreshToken()).isNotEqualTo(original);
			assertThat(repository.findByUserId(userId)).contains(first.refreshToken());
			assertThat(jwtTokenProvider.validateToken(first.accessToken())).isTrue();
			assertThat(jwtTokenProvider.validateToken(first.refreshToken())).isTrue();
		} finally {
			start.countDown();
			executor.shutdownNow();
		}
	}

	@Test
	void retryAfterFiveSecondsReusesPairButFailsAfterElevenSeconds() throws Exception {
		String original = login();
		TokenResponse issued = service.refreshToken(original);
		Thread.sleep(5000);
		assertThat(service.refreshToken(original)).isEqualTo(issued);
		assertThat(redisTemplate.getExpire(graceKey(), TimeUnit.MILLISECONDS)).isBetween(1L, 5000L);
		Thread.sleep(6000);
		assertError(original, ErrorCode.REFRESH_TOKEN_MISMATCH);
		assertThat(redisTemplate.hasKey(graceKey())).isFalse();
	}

	@Test
	void normalRotationPreservesResponseAndRefreshTtl() {
		String original = login();
		TokenResponse issued = service.refreshToken(original);
		assertThat(issued.tokenType()).isEqualTo("Bearer");
		assertThat(repository.findByUserId(userId)).contains(issued.refreshToken());
		assertThat(redisTemplate.getExpire("refresh_token:" + userId, TimeUnit.MILLISECONDS))
				.isBetween(REFRESH_TTL - 5000, REFRESH_TTL);
	}

	@Test
	void tokenTwoGenerationsBackIsRejected() {
		String first = login();
		TokenResponse second = service.refreshToken(first);
		TokenResponse third = service.refreshToken(second.refreshToken());
		assertError(first, ErrorCode.REFRESH_TOKEN_MISMATCH);
		assertThat(service.refreshToken(second.refreshToken())).isEqualTo(third);
	}

	@Test
	void logoutDeletesCurrentTokenAndGracePair() {
		userId = "987654321";
		String first = login();
		TokenResponse second = service.refreshToken(first);
		service.logout(new CustomUserDetails(Long.valueOf(userId), "test@example.com", null));
		assertError(first, ErrorCode.REFRESH_TOKEN_NOT_FOUND);
		assertError(second.refreshToken(), ErrorCode.REFRESH_TOKEN_NOT_FOUND);
		assertThat(repository.existsByUserId(userId)).isFalse();
		assertThat(redisTemplate.hasKey(graceKey())).isFalse();
	}

	@Test
	void loginInvalidatesPreviousSessionAndGracePair() {
		String first = login();
		TokenResponse second = service.refreshToken(first);
		String newSession = login();
		assertError(first, ErrorCode.REFRESH_TOKEN_MISMATCH);
		assertError(second.refreshToken(), ErrorCode.REFRESH_TOKEN_MISMATCH);
		assertThat(redisTemplate.hasKey(graceKey())).isFalse();
		assertThat(service.refreshToken(newSession).refreshToken()).isNotEqualTo(newSession);
	}

	@Test
	void missingStoredTokenReturnsA005() {
		assertError(jwtTokenProvider.createRefreshToken(userId), ErrorCode.REFRESH_TOKEN_NOT_FOUND);
	}

	@Test
	void forgedTokenReturnsA002() {
		assertError("forged-token", ErrorCode.INVALID_TOKEN);
	}

	@Test
	void expiredTokenReturnsA002() {
		ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", -10000L);
		String expired = jwtTokenProvider.createRefreshToken(userId);
		assertError(expired, ErrorCode.INVALID_TOKEN);
	}

	@Test
	void existingTokenWithoutJtiCanStillBeRotated() {
		String legacy =
				Jwts.builder()
						.subject(userId)
						.issuedAt(new Date())
						.expiration(new Date(System.currentTimeMillis() + REFRESH_TTL))
						.signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
						.compact();
		repository.save(userId, legacy, REFRESH_TTL);
		TokenResponse issued = service.refreshToken(legacy);
		assertThat(issued.refreshToken()).isNotEqualTo(legacy);
		assertThat(service.refreshToken(legacy)).isEqualTo(issued);
	}

	@Test
	void refreshTokensAreUniqueEvenWhenIssuedImmediately() {
		assertThat(jwtTokenProvider.createRefreshToken(userId))
				.isNotEqualTo(jwtTokenProvider.createRefreshToken(userId));
	}

	@Test
	void zeroReuseIntervalDisablesGrace() {
		ReflectionTestUtils.setField(service, "refreshTokenReuseInterval", 0L);
		String first = login();
		service.refreshToken(first);
		assertError(first, ErrorCode.REFRESH_TOKEN_MISMATCH);
		assertThat(redisTemplate.hasKey(graceKey())).isFalse();
	}

	@Test
	void graceStoresPreviousHashAndExactPairForTenSeconds() throws Exception {
		String original = login();
		TokenResponse issued = service.refreshToken(original);
		Map<String, String> cached = new HashMap<>();
		try (var connection = connectionFactory.getConnection()) {
			connection
					.hashCommands()
					.hGetAll(graceKey().getBytes(StandardCharsets.UTF_8))
					.forEach(
							(key, value) ->
									cached.put(
											new String(key, StandardCharsets.UTF_8),
											new String(value, StandardCharsets.UTF_8)));
		}
		String expectedHash =
				HexFormat.of()
						.formatHex(
								MessageDigest.getInstance("SHA-256")
										.digest(original.getBytes(StandardCharsets.UTF_8)));
		assertThat(cached).containsOnlyKeys("prev", "pair");
		assertThat(cached.get("prev")).isEqualTo(expectedHash).isNotEqualTo(original);
		assertThat(new ObjectMapper().readValue(cached.get("pair"), TokenResponse.class))
				.isEqualTo(issued);
		assertThat(redisTemplate.getExpire(graceKey(), TimeUnit.MILLISECONDS))
				.isBetween(1L, 10000L);
	}

	@Test
	void httpErrorCodesAndStatusesRemainUnchanged() throws Exception {
		assertHttpError("forged-token", "A002");
		String first = jwtTokenProvider.createRefreshToken(userId);
		assertHttpError(first, "A005");
		repository.save(userId, first, REFRESH_TTL);
		service.refreshToken(first);
		login();
		assertHttpError(first, "A006");
		ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", -10000L);
		assertHttpError(jwtTokenProvider.createRefreshToken(userId), "A002");
	}

	private TokenResponse refreshThroughHttp(String token) throws Exception {
		String body =
				mockMvc.perform(
								post("/api/auth/refresh")
										.contentType(MediaType.APPLICATION_JSON)
										.content(
												new ObjectMapper()
														.writeValueAsString(
																Map.of("refreshToken", token))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value("SUCCESS"))
						.andExpect(jsonPath("$.data.tokenType").value("Bearer"))
						.andReturn()
						.getResponse()
						.getContentAsString(StandardCharsets.UTF_8);
		var data = new ObjectMapper().readTree(body).get("data");
		assertThat(data.size()).isEqualTo(3);
		return new ObjectMapper().treeToValue(data, TokenResponse.class);
	}

	private void assertHttpError(String token, String code) throws Exception {
		mockMvc.perform(
						post("/api/auth/refresh")
								.contentType(MediaType.APPLICATION_JSON)
								.content(
										new ObjectMapper()
												.writeValueAsString(Map.of("refreshToken", token))))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value(code));
	}

	private String login() {
		String token = jwtTokenProvider.createRefreshToken(userId);
		repository.save(userId, token, REFRESH_TTL);
		return token;
	}

	private String graceKey() {
		return "refresh_token_grace:" + userId;
	}

	private void assertError(String token, ErrorCode error) {
		assertThatThrownBy(() -> service.refreshToken(token))
				.isInstanceOfSatisfying(
						GlobalException.class, e -> assertThat(e.getErrorCode()).isEqualTo(error));
	}
}
