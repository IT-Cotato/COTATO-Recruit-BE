package org.cotato.backend.recruit.admin.dto.request.email;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "모집 알림 메일 내용 저장 요청")
public record RecruitmentNotificationEmailRequest(
		@Schema(description = "기수 ID (미입력시 현재 모집 중인 기수)", example = "1") Long generationId,
		@Schema(description = "메일 내용", example = "모집이 시작되었습니다!")
				@NotBlank(message = "메일 내용은 필수입니다.")
				String content) {}
