package org.cotato.backend.recruit.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.auth.dto.CustomUserDetails;
import org.cotato.backend.recruit.auth.dto.TokenResponse;
import org.cotato.backend.recruit.auth.dto.UserInfoResponse;
import org.cotato.backend.recruit.auth.jwt.JwtTokenProvider;
import org.cotato.backend.recruit.auth.repository.RefreshTokenRepository;
import org.cotato.backend.recruit.common.error.ErrorCode;
import org.cotato.backend.recruit.common.exception.GlobalException;
import org.cotato.backend.recruit.domain.user.entity.User;
import org.cotato.backend.recruit.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	@Value("${jwt.refresh-token-reuse-interval:10000}")
	private long refreshTokenReuseInterval;

	/**
	 * Refresh Token을 사용하여 새로운 Access Token 발급
	 *
	 * @param refreshToken Refresh Token
	 * @return 새로운 Access Token과 Refresh Token
	 */
	public TokenResponse refreshToken(String refreshToken) {
		// Refresh Token 유효성 검증
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			throw new GlobalException(ErrorCode.INVALID_TOKEN);
		}

		// Refresh Token에서 사용자 ID 추출
		String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

		// 동시 요청 중 실제 회전에 성공한 요청의 토큰 쌍만 사용한다.
		TokenResponse issued =
				TokenResponse.of(
						jwtTokenProvider.createAccessToken(userId),
						jwtTokenProvider.createRefreshToken(userId));
		try {
			String result =
					refreshTokenRepository.rotate(
							userId,
							refreshToken,
							sha256(refreshToken),
							issued.refreshToken(),
							jwtTokenProvider.getRefreshTokenExpiration(),
							objectMapper.writeValueAsString(issued),
							refreshTokenReuseInterval);
			if (result == null) {
				throw new GlobalException(ErrorCode.INTERNAL_SERVER_ERROR);
			}
			return switch (result) {
				case "ROTATED" -> issued;
				case "NOT_FOUND" -> throw new GlobalException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
				case "MISMATCH" -> throw new GlobalException(ErrorCode.REFRESH_TOKEN_MISMATCH);
				default -> objectMapper.readValue(result, TokenResponse.class);
			};
		} catch (JsonProcessingException e) {
			throw new GlobalException(ErrorCode.INTERNAL_SERVER_ERROR, e);
		}
	}

	private static String sha256(String value) {
		try {
			return HexFormat.of()
					.formatHex(
							MessageDigest.getInstance("SHA-256")
									.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 로그아웃 - Redis에서 Refresh Token 삭제
	 *
	 * @param userDetails 인증된 사용자 정보
	 */
	public void logout(CustomUserDetails userDetails) {
		String userId = String.valueOf(userDetails.getUserId());

		// Redis에서 Refresh Token 삭제
		refreshTokenRepository.deleteByUserId(userId);
	}

	/**
	 * 현재 로그인한 사용자 정보 조회
	 *
	 * @param userDetails 인증된 사용자 정보
	 * @return 사용자 정보 (userId, email, name, role)
	 */
	public UserInfoResponse getCurrentUserInfo(CustomUserDetails userDetails) {
		User user =
				userRepository
						.findById(userDetails.getUserId())
						.orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

		return UserInfoResponse.from(user);
	}
}
