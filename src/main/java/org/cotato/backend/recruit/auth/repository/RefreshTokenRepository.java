package org.cotato.backend.recruit.auth.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

	private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
	private static final String REFRESH_TOKEN_GRACE_PREFIX = "refresh_token_grace:";

	private static final RedisScript<String> SAVE_SCRIPT =
			new DefaultRedisScript<>(
					"""
			redis.call('SET', KEYS[1], ARGV[1], 'PX', ARGV[2])
			redis.call('DEL', KEYS[2])
			return 'OK'
			""",
					String.class);

	private static final RedisScript<String> ROTATE_SCRIPT =
			new DefaultRedisScript<>(
					"""
			local current = redis.call('GET', KEYS[1])
			if not current then
				return 'NOT_FOUND'
			end
			if current == ARGV[1] then
				redis.call('SET', KEYS[1], ARGV[2], 'PX', ARGV[3])
				redis.call('DEL', KEYS[2])
				if tonumber(ARGV[6]) > 0 then
					redis.call('HSET', KEYS[2], 'prev', ARGV[4], 'pair', ARGV[5])
					redis.call('PEXPIRE', KEYS[2], ARGV[6])
				end
				return 'ROTATED'
			end
			if redis.call('HGET', KEYS[2], 'prev') == ARGV[4] then
				return redis.call('HGET', KEYS[2], 'pair')
			end
			return 'MISMATCH'
			""",
					String.class);

	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * Refresh Token 저장
	 *
	 * @param userId 사용자 ID
	 * @param refreshToken Refresh Token
	 * @param expirationMillis 만료 시간 (밀리초)
	 */
	public void save(String userId, String refreshToken, long expirationMillis) {
		if (expirationMillis <= 0) {
			throw new IllegalArgumentException("Refresh token expiration must be positive");
		}
		redisTemplate.execute(
				SAVE_SCRIPT, keys(userId), refreshToken, String.valueOf(expirationMillis));
	}

	/** 현재 토큰 비교, 회전 및 직전 토큰 유예 처리를 하나의 Redis 스크립트로 실행한다. */
	public String rotate(
			String userId,
			String presentedToken,
			String presentedTokenHash,
			String newRefreshToken,
			long expirationMillis,
			String newPairJson,
			long graceMillis) {
		if (expirationMillis <= 0 || graceMillis < 0) {
			throw new IllegalArgumentException(
					"Invalid refresh token expiration or reuse interval");
		}
		return redisTemplate.execute(
				ROTATE_SCRIPT,
				keys(userId),
				presentedToken,
				newRefreshToken,
				String.valueOf(expirationMillis),
				presentedTokenHash,
				newPairJson,
				String.valueOf(graceMillis));
	}

	private List<String> keys(String userId) {
		return List.of(REFRESH_TOKEN_PREFIX + userId, REFRESH_TOKEN_GRACE_PREFIX + userId);
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
		redisTemplate.delete(keys(userId));
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
