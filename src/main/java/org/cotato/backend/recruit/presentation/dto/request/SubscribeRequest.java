package org.cotato.backend.recruit.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "모집 알림 구독 신청 요청")
public record SubscribeRequest(
		@Schema(description = "이메일 주소", example = "example@gmail.com")
				@NotBlank(message = "이메일은 필수입니다.")
				@Email(message = "올바른 이메일 형식이 아닙니다.")
				String email) {}
