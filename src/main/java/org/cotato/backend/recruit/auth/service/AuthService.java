package org.cotato.backend.recruit.auth.service;

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
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;

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

		// Redis에서 저장된 Refresh Token 조회
		String storedRefreshToken =
				refreshTokenRepository
						.findByUserId(userId)
						.orElseThrow(
								() -> {
									log.error("Refresh token not found for user: {}", userId);
									return new GlobalException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
								});

		// Refresh Token 일치 여부 확인
		if (!storedRefreshToken.equals(refreshToken)) {
			log.error("Refresh token mismatch for user: {}", userId);
			throw new GlobalException(ErrorCode.REFRESH_TOKEN_MISMATCH);
		}

		// 새로운 토큰 발급
		String newAccessToken = jwtTokenProvider.createAccessToken(userId);
		String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

		// Redis에 새로운 Refresh Token 저장
		refreshTokenRepository.save(
				userId, newRefreshToken, jwtTokenProvider.getRefreshTokenExpiration());

		return TokenResponse.of(newAccessToken, newRefreshToken);
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
