package org.cotato.backend.recruit.admin.dto.request.staffEvaluation;

import org.cotato.backend.recruit.domain.evaluation.enums.EvaluatorType;

public record CreateStaffEvaluationRequest(EvaluatorType evaluatorType, String comment) {}
