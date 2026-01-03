package org.cotato.backend.recruit.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모집 활성화 여부 응답")
public record RecruitmentStatusResponse(
		@Schema(description = "모집 활성화 여부", example = "true") boolean isActive) {

	public static RecruitmentStatusResponse of(boolean isActive) {
		return new RecruitmentStatusResponse(isActive);
	}
}
