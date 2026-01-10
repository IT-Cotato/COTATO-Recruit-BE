package org.cotato.backend.recruit.admin.dto.response.applicationQuestion;

import org.cotato.backend.recruit.domain.question.entity.Question;

public record ApplicationQuestionResponse(Integer sequence, String content, Integer maxByte) {
	public static ApplicationQuestionResponse from(Question question) {
		return new ApplicationQuestionResponse(
				question.getSequence(), question.getContent(), question.getMaxByte());
	}
}
