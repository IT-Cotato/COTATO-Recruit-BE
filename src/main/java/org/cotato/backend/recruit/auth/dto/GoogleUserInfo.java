package org.cotato.backend.recruit.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Google OAuth2 인증 과정에서 구글로부터 받는 사용자 정보를 매핑하는 DTO */
public record GoogleUserInfo(
		@JsonProperty("sub") String id,
		@JsonProperty("email") String email,
		@JsonProperty("name") String name,
		@JsonProperty("email_verified") Boolean emailVerified) {}
