package org.cotato.backend.recruit.admin.dto.request.staffEvaluation;

import jakarta.validation.constraints.NotNull;
import org.cotato.backend.recruit.domain.evaluation.enums.EvaluatorType;

public record CreateStaffEvaluationRequest(
		@NotNull EvaluatorType evaluatorType, @NotNull String comment) {}
