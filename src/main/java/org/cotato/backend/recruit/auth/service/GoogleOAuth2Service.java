package org.cotato.backend.recruit.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.auth.dto.GoogleTokenResponse;
import org.cotato.backend.recruit.auth.dto.GoogleUserInfo;
import org.cotato.backend.recruit.auth.dto.TokenResponse;
import org.cotato.backend.recruit.auth.jwt.JwtTokenProvider;
import org.cotato.backend.recruit.auth.repository.RefreshTokenRepository;
import org.cotato.backend.recruit.common.error.ErrorCode;
import org.cotato.backend.recruit.common.exception.GlobalException;
import org.cotato.backend.recruit.domain.user.entity.User;
import org.cotato.backend.recruit.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuth2Service {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;
	private final RestTemplate restTemplate;

	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.google.client-secret}")
	private String clientSecret;

	private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
	private static final String GOOGLE_USER_INFO_URL =
			"https://www.googleapis.com/oauth2/v3/userinfo";

	/**
	 * Google OAuth2 로그인
	 *
	 * <p>플로우: Authorization Code → Google Access Token → 사용자 정보 조회 → DB 확인/저장 → JWT 발급
	 *
	 * @param code Authorization Code (프론트엔드에서 받은 코드)
	 * @param redirectUri 프론트엔드 Redirect URI
	 * @return JWT 토큰 (Access Token, Refresh Token)
	 */
	@Transactional
	public TokenResponse loginWithGoogle(String code, String redirectUri) {
		String googleAccessToken = getGoogleAccessToken(code, redirectUri);
		GoogleUserInfo userInfo = getGoogleUserInfo(googleAccessToken);
		User user = getOrCreateUser(userInfo);

		String userId = String.valueOf(user.getId());
		String accessToken = jwtTokenProvider.createAccessToken(userId);
		String refreshToken = jwtTokenProvider.createRefreshToken(userId);

		refreshTokenRepository.save(
				userId, refreshToken, jwtTokenProvider.getRefreshTokenExpiration());

		return TokenResponse.of(accessToken, refreshToken);
	}

	/**
	 * Authorization Code로 Google Access Token 발급
	 *
	 * @param code Authorization Code
	 * @param redirectUri Redirect URI
	 * @return Google Access Token
	 */
	private String getGoogleAccessToken(String code, String redirectUri) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("code", code);
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		params.add("redirect_uri", redirectUri);
		params.add("grant_type", "authorization_code");

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

		try {
			ResponseEntity<GoogleTokenResponse> response =
					restTemplate.exchange(
							GOOGLE_TOKEN_URL, HttpMethod.POST, request, GoogleTokenResponse.class);

			if (response.getBody() == null || response.getBody().accessToken() == null) {
				log.error("Failed to get Google access token - empty response");
				throw new GlobalException(ErrorCode.OAUTH2_AUTHENTICATION_FAILED);
			}

			return response.getBody().accessToken();

		} catch (Exception e) {
			log.error("Failed to exchange authorization code for access token", e);
			throw new GlobalException(ErrorCode.OAUTH2_AUTHENTICATION_FAILED);
		}
	}

	/**
	 * Google Access Token으로 사용자 정보 조회
	 *
	 * @param accessToken Google Access Token
	 * @return 사용자 정보
	 */
	private GoogleUserInfo getGoogleUserInfo(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);

		HttpEntity<Void> request = new HttpEntity<>(headers);

		try {
			ResponseEntity<GoogleUserInfo> response =
					restTemplate.exchange(
							GOOGLE_USER_INFO_URL, HttpMethod.GET, request, GoogleUserInfo.class);

			if (response.getBody() == null) {
				log.error("Failed to get Google user info - empty response");
				throw new GlobalException(ErrorCode.OAUTH2_AUTHENTICATION_FAILED);
			}

			return response.getBody();

		} catch (Exception e) {
			log.error("Failed to get user info from Google", e);
			throw new GlobalException(ErrorCode.OAUTH2_AUTHENTICATION_FAILED);
		}
	}

	/**
	 * DB에서 사용자 확인 후 회원가입 또는 로그인 처리
	 *
	 * @param userInfo Google 사용자 정보
	 * @return User 엔티티
	 */
	private User getOrCreateUser(GoogleUserInfo userInfo) {
		return userRepository
				.findByProviderAndProviderId(User.Provider.GOOGLE, userInfo.id())
				.map(
						existingUser -> {
							existingUser.updateProfile(userInfo.name());
							return userRepository.save(existingUser);
						})
				.orElseGet(
						() -> {
							User newUser =
									User.createGoogleUser(
											userInfo.email(), userInfo.name(), userInfo.id());
							return userRepository.save(newUser);
						});
	}
}
