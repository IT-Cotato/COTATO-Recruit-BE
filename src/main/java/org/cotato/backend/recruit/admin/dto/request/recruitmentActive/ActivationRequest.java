package org.cotato.backend.recruit.admin.dto.request.recruitmentActive;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ActivationRequest(
		@NotNull Long generation,
		@NotNull Boolean isAdditionalRecruitmentActive,
		@NotNull LocalDate startDate,
		@NotNull LocalDate endDate) {}
