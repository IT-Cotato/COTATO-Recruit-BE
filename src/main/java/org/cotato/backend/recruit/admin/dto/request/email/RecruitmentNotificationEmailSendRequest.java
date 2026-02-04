package org.cotato.backend.recruit.admin.dto.request.email;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모집 알림 메일 전송 요청")
public record RecruitmentNotificationEmailSendRequest(
		@Schema(description = "기수 ID (미입력시 현재 모집 중인 기수)", example = "1") Long generationId) {}
