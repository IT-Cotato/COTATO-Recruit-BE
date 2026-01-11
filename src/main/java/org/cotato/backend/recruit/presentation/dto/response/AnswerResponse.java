package org.cotato.backend.recruit.presentation.dto.response;

import org.cotato.backend.recruit.domain.application.entity.ApplicationAnswer;

public record AnswerResponse(Long answerId, Long questionId, String content) {

	public static AnswerResponse from(ApplicationAnswer answer) {
		return new AnswerResponse(
				answer.getId(), answer.getQuestion().getId(), answer.getContent());
	}
}
