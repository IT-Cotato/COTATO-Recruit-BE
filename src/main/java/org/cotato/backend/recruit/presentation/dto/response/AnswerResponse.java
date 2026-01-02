package org.cotato.backend.recruit.presentation.dto.response;

import org.cotato.backend.recruit.domain.application.entity.ApplicationAnswer;

public record AnswerResponse(
		Long answerId,
		Long questionId,
		String answerType,
		Boolean isChecked,
		String content,
		String fileKey,
		String fileUrl) {

	public static AnswerResponse from(ApplicationAnswer answer) {
		return new AnswerResponse(
				answer.getId(),
				answer.getQuestion().getId(),
				answer.getAnswerType().name(),
				answer.getIsChecked(),
				answer.getContent(),
				answer.getFileKey(),
				answer.getFileUrl());
	}
}
