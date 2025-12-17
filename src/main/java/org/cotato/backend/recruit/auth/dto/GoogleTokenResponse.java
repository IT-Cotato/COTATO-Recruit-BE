package org.cotato.backend.recruit.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Google OAuth2 인증 과정에서 구글로부터 받는 토큰 응답을 매핑하는 DTO */
public record GoogleTokenResponse(
		@JsonProperty("access_token") String accessToken, // 액세스 토큰
		@JsonProperty("expires_in") Integer expiresIn, // 액세스 토큰 만료 시간 (초)
		@JsonProperty("scope") String scope, // 액세스 토큰 권한 범위
		@JsonProperty("token_type") String tokenType, // 토큰 타입 (예: Bearer)
		@JsonProperty("id_token") String idToken) {} // ID 토큰 (사용자 정보 포함)
