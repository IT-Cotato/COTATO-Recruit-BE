package org.cotato.backend.recruit.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.auth.dto.CustomUserDetails;
import org.cotato.backend.recruit.auth.dto.GoogleLoginRequest;
import org.cotato.backend.recruit.auth.dto.TokenRefreshRequest;
import org.cotato.backend.recruit.auth.dto.TokenResponse;
import org.cotato.backend.recruit.auth.service.AuthService;
import org.cotato.backend.recruit.auth.service.GoogleOAuth2Service;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증 API", description = "인증 및 토큰 관리 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final GoogleOAuth2Service googleOAuth2Service;

	@Operation(summary = "구글 로그인", description = "프론트엔드에서 받은 Authorization Code로 JWT 토큰을 발급합니다.")
	@PostMapping("/login/google")
	public ApiResponse<TokenResponse> loginWithGoogle(
			@Parameter(description = "구글 로그인 요청 (Authorization Code)", required = true)
					@Valid
					@RequestBody
					GoogleLoginRequest request) {
		TokenResponse response =
				googleOAuth2Service.loginWithGoogle(request.code(), request.redirectUri());
		return ApiResponse.success(response);
	}

	@Operation(
			summary = "토큰 갱신",
			description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급합니다.")
	@PostMapping("/refresh")
	public ApiResponse<TokenResponse> refreshToken(
			@Parameter(description = "Refresh Token 요청", required = true) @Valid @RequestBody
					TokenRefreshRequest request) {
		TokenResponse response = authService.refreshToken(request.refreshToken());
		return ApiResponse.success(response);
	}

	@Operation(summary = "로그아웃", description = "사용자를 로그아웃하고 Redis에 저장된 Refresh Token을 삭제합니다.")
	@PostMapping("/logout")
	public ApiResponse<Void> logout(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
		authService.logout(userDetails);
		return ApiResponse.success();
	}
}
