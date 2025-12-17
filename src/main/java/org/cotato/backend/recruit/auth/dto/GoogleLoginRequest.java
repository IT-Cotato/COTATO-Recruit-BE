package org.cotato.backend.recruit.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
		@NotBlank(message = "Authorization code는 필수입니다.") String code,
		@NotBlank(message = "Redirect URI는 필수입니다.") String redirectUri) {}
