package org.cotato.backend.recruit.auth.repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

	private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * Refresh Token 저장
	 *
	 * @param userId 사용자 ID
	 * @param refreshToken Refresh Token
	 * @param expirationMillis 만료 시간 (밀리초)
	 */
	public void save(String userId, String refreshToken, long expirationMillis) {
		String key = REFRESH_TOKEN_PREFIX + userId;
		redisTemplate.opsForValue().set(key, refreshToken, expirationMillis, TimeUnit.MILLISECONDS);
	}

	/**
	 * 사용자 ID로 Refresh Token 조회
	 *
	 * @param userId 사용자 ID
	 * @return Refresh Token (Optional)
	 */
	public Optional<String> findByUserId(String userId) {
		String key = REFRESH_TOKEN_PREFIX + userId;
		String refreshToken = redisTemplate.opsForValue().get(key);
		return Optional.ofNullable(refreshToken);
	}

	/**
	 * Refresh Token 삭제
	 *
	 * @param userId 사용자 ID
	 */
	public void deleteByUserId(String userId) {
		String key = REFRESH_TOKEN_PREFIX + userId;
		redisTemplate.delete(key);
	}

	/**
	 * Refresh Token 존재 여부 확인
	 *
	 * @param userId 사용자 ID
	 * @return 존재 여부
	 */
	public boolean existsByUserId(String userId) {
		String key = REFRESH_TOKEN_PREFIX + userId;
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}
}
