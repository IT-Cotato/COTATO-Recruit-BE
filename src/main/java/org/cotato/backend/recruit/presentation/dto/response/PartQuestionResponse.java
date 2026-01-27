package org.cotato.backend.recruit.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.cotato.backend.recruit.common.util.LengthManager;
import org.cotato.backend.recruit.domain.question.entity.Question;

@Schema(description = "파트별 질문 및 답변 목록 응답")
public record PartQuestionResponse(
		@Schema(description = "질문 및 답변 리스트") List<QuestionWithAnswerResponse> questionsWithAnswers,
		@Schema(description = "첨부된 PDF 파일 URL") String pdfFileUrl,
		@Schema(description = "첨부된 PDF 파일 키") String pdfFileKey) {

	public static PartQuestionResponse of(
			List<QuestionWithAnswerResponse> questionsWithAnswers,
			String pdfFileUrl,
			String pdfFileKey) {
		return new PartQuestionResponse(questionsWithAnswers, pdfFileUrl, pdfFileKey);
	}

	@Schema(description = "질문과 저장된 답변 응답")
	public record QuestionWithAnswerResponse(
			@Schema(description = "질문 ID", example = "1") Long questionId,
			@Schema(description = "질문 순서", example = "1") Integer sequence,
			@Schema(description = "질문 내용", example = "COTATO에 지원하게 된 동기를 작성해 주세요.") String content,
			@Schema(description = "파트 타입", example = "COMMON") String partType,
			@Schema(description = "응답 글자수", example = "100") Integer length,
			@Schema(description = "최대 입력 글자수", example = "500") Integer maxLength,
			@Schema(description = "저장된 답변 (없으면 null)") AnswerResponse savedAnswer) {
		public static QuestionWithAnswerResponse of(Question question, AnswerResponse savedAnswer) {
			return new QuestionWithAnswerResponse(
					question.getId(),
					question.getSequence(),
					question.getContent(),
					question.getQuestionType().name(),
					LengthManager.getCharacterCount(savedAnswer.content()),
					question.getMaxLength(),
					savedAnswer);
		}
	}
}
