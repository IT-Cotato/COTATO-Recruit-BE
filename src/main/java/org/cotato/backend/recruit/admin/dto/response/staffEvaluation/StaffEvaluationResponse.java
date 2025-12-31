package org.cotato.backend.recruit.admin.dto.response.staffEvaluation;

import lombok.Builder;

@Builder
public record StaffEvaluationResponse(String comment) {
	public static StaffEvaluationResponse from(String comment) {
		return StaffEvaluationResponse.builder().comment(comment).build();
	}
}
