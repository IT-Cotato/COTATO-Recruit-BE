package org.cotato.backend.recruit.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모집 활성화 여부 응답")
public record RecruitmentStatusResponse(
		@Schema(description = "모집 활성화 여부", example = "true") boolean isActive,
		@Schema(description = "모집 중인 기수 (모집 비활성화 시 null)", example = "10", nullable = true)
				Long generationId) {

	public static RecruitmentStatusResponse of(boolean isActive, Long generationId) {
		return new RecruitmentStatusResponse(isActive, generationId);
	}
}
