package org.cotato.backend.recruit.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** 토큰 갱신 요청을 위한 DTO */
public record TokenRefreshRequest(
		@NotBlank(message = "Refresh Token은 필수입니다.") String refreshToken) {}
