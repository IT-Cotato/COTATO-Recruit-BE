package org.cotato.backend.recruit.presentation.dto.response;

public record AnswerResponse(
		Long answerId,
		Long questionId,
		String answerType,
		Boolean isChecked,
		String content,
		String fileKey,
		String fileUrl) {}
