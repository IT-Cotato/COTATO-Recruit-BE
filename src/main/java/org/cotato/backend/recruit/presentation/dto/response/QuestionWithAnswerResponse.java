package org.cotato.backend.recruit.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.cotato.backend.recruit.domain.question.entity.Question;

@Schema(description = "질문과 저장된 답변 응답")
public record QuestionWithAnswerResponse(
		@Schema(description = "질문 ID", example = "1") Long questionId,
		@Schema(description = "질문 순서", example = "1") Integer sequence,
		@Schema(description = "질문 내용", example = "COTATO에 지원하게 된 동기를 작성해 주세요.") String content,
		@Schema(description = "파트 타입", example = "COMMON") String partType,
		@Schema(
						description = "답변 타입 (CHECKBOX: 체크박스, TEXT: 텍스트, FILE: 파일)",
						example = "TEXT",
						allowableValues = {"CHECKBOX", "TEXT", "FILE"})
				String answerType,
		@Schema(description = "최대 입력 바이트 수", example = "500") Integer maxByte,
		@Schema(description = "저장된 답변 (없으면 null)") AnswerResponse savedAnswer) {

	public static QuestionWithAnswerResponse of(Question question, AnswerResponse savedAnswer) {
		return new QuestionWithAnswerResponse(
				question.getId(),
				question.getSequence(),
				question.getContent(),
				question.getPartType().name(),
				question.getAnswerType().name(),
				question.getMaxByte(),
				savedAnswer);
	}
}
