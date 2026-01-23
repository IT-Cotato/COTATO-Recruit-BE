package org.cotato.backend.recruit.admin.dto.request.recruitmentActive;

import jakarta.validation.constraints.NotNull;

public record ActivationRequest(
		@NotNull Long generationId,
		@NotNull Boolean isAdditionalRecruitmentActive) {
}
