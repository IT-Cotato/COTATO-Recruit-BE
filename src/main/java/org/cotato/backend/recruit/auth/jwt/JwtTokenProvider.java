package org.cotato.backend.recruit.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String secretKey;

	@Value("${jwt.access-token-expiration}")
	private long accessTokenExpiration;

	@Value("${jwt.refresh-token-expiration}")
	private long refreshTokenExpiration;

	private SecretKey key;

	@PostConstruct
	protected void init() {
		key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Access Token 생성
	 *
	 * @param userId 사용자 ID
	 * @return Access Token
	 */
	public String createAccessToken(String userId) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

		return Jwts.builder()
				.subject(userId) // 사용자 ID 설정
				.issuedAt(now) // 발급 시간 설정
				.expiration(expiryDate) // 만료 시간 설정
				.signWith(key) // 서명에 사용할 키 설정 (위변조 방지)
				.compact();
	}

	/**
	 * Refresh Token 생성
	 *
	 * @param userId 사용자 ID
	 * @return Refresh Token
	 */
	public String createRefreshToken(String userId) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

		return Jwts.builder()
				.subject(userId) // 사용자 ID 설정
				.issuedAt(now) // 발급 시간 설정
				.expiration(expiryDate) // 만료 시간 설정
				.signWith(key) // 서명에 사용할 키 설정 (위변조 방지)
				.compact();
	}

	/**
	 * JWT 토큰에서 사용자 ID 추출
	 *
	 * @param token JWT 토큰
	 * @return 사용자 ID
	 */
	public String getUserIdFromToken(String token) {
		Claims claims =
				Jwts.parser()
						.verifyWith(key) // 서명 검증
						.build()
						.parseSignedClaims(token) // 토큰 파싱
						.getPayload(); // 클레임(페이로드) 추출
		return claims.getSubject();
	}

	/**
	 * JWT 토큰 유효성 검증
	 *
	 * @param token JWT 토큰
	 * @return 유효 여부
	 */
	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
			return true;
		} catch (SecurityException | MalformedJwtException e) {
			log.error("Invalid JWT signature", e);
		} catch (ExpiredJwtException e) {
			log.error("Expired JWT token", e);
		} catch (UnsupportedJwtException e) {
			log.error("Unsupported JWT token", e);
		} catch (IllegalArgumentException e) {
			log.error("JWT claims string is empty", e);
		}
		return false;
	}

	/**
	 * Refresh Token 만료 시간 조회
	 *
	 * @return Refresh Token 만료 시간 (밀리초)
	 */
	public long getRefreshTokenExpiration() {
		return refreshTokenExpiration;
	}
}
