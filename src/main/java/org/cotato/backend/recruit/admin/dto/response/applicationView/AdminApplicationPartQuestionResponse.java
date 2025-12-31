package org.cotato.backend.recruit.admin.dto.response.applicationView;

import lombok.Builder;
import org.cotato.backend.recruit.domain.application.enums.AnswerType;

@Builder
public record AdminApplicationPartQuestionResponse(
		Integer sequence,
		String questionContent,
		AnswerType answerType,
		Boolean isChecked,
		String content,
		String fileKey,
		String fileUrl) {}
