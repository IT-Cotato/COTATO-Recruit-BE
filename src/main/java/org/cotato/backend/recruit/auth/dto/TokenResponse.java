package org.cotato.backend.recruit.auth.dto;

/** 인증 토큰 응답을 나타내는 DTO */
public record TokenResponse(String accessToken, String refreshToken, String tokenType) {

	public static TokenResponse of(String accessToken, String refreshToken) {
		return new TokenResponse(accessToken, refreshToken, "Bearer");
	}
}
