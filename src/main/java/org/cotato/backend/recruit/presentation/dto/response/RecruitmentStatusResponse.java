package org.cotato.backend.recruit.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Optional;
import org.cotato.backend.recruit.domain.generation.entity.Generation;

@Schema(description = "모집 활성화 여부 응답")
public record RecruitmentStatusResponse(
		@Schema(description = "모집 활성화 여부", example = "true") boolean isActive,
		@Schema(description = "모집 중인 기수 (모집 비활성화 시 null)", example = "10", nullable = true)
				Long generationId,
		@Schema(description = "추가 모집 활성화 여부", example = "true")
				boolean isAdditionalRecruitmentActive) {

	public static RecruitmentStatusResponse of(Optional<Generation> generation) {
		return new RecruitmentStatusResponse(
				generation.isPresent() ? generation.get().isRecruitingActive() : false,
				generation.isPresent() ? generation.get().getId() : null,
				generation.isPresent() ? generation.get().isAdditionalRecruitmentActive() : false);
	}
}
