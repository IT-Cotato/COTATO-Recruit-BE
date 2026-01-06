package org.cotato.backend.recruit.admin.dto.response.applicationView;

import lombok.Builder;
import org.cotato.backend.recruit.common.util.ByteManager;
import org.cotato.backend.recruit.domain.application.entity.ApplicationAnswer;
import org.cotato.backend.recruit.domain.application.enums.AnswerType;
import org.cotato.backend.recruit.domain.question.entity.Question;

@Builder
public record AdminApplicationPartQuestionResponse(
		Integer sequence,
		String questionContent,
		AnswerType answerType,
		Boolean isChecked,
		String content,
		String fileKey,
		String fileUrl,
		int byteSize) {

	public static AdminApplicationPartQuestionResponse from(
			Question question, ApplicationAnswer answer) {
		String content = (answer != null ? answer.getContent() : null);

		return AdminApplicationPartQuestionResponse.builder()
				.sequence(question.getSequence())
				.questionContent(question.getContent())
				// answer가 null일 수 있는 로직 처리
				.answerType(answer != null ? answer.getAnswerType() : question.getAnswerType())
				.isChecked(answer != null ? answer.getIsChecked() : null)
				.content(content)
				.fileKey(answer != null ? answer.getFileKey() : null)
				.fileUrl(answer != null ? answer.getFileUrl() : null)
				.byteSize(content != null ? ByteManager.getByteSize(content) : 0)
				.build();
	}
}
