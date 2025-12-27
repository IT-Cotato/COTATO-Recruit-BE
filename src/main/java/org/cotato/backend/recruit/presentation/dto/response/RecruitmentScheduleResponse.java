package org.cotato.backend.recruit.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모집 일정 응답")
public record RecruitmentScheduleResponse(
		@Schema(description = "기수 ID", example = "13") Long generationId,
		@Schema(description = "지원 기간", example = "2025년 1월 1일 00:00 ~ 2025년 1월 7일 23:59")
				String applicationPeriod,
		@Schema(description = "서류 발표", example = "2025년 1월 10일 18:00") String documentAnnouncement,
		@Schema(description = "면접 평가", example = "2025년 1월 13일 10:00 ~ 2025년 1월 14일 18:00")
				String interview,
		@Schema(description = "최종 발표", example = "2025년 1월 17일 18:00") String finalAnnouncement) {}
