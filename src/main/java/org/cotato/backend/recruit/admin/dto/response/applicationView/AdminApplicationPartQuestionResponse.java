package org.cotato.backend.recruit.admin.dto.response.applicationView;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import org.cotato.backend.recruit.common.util.ByteManager;
import org.cotato.backend.recruit.domain.application.entity.ApplicationAnswer;
import org.cotato.backend.recruit.domain.question.entity.Question;

@Schema(description = "관리자용 지원서 파트별 답변 및 파일 응답")
public record AdminApplicationPartQuestionResponse(
		@Schema(description = "파트별 질문/답변 리스트") List<AdminPartQuestionResponse> questionsWithAnswers,
		@Schema(description = "첨부된 PDF 파일 URL") String pdfFileUrl,
		@Schema(description = "첨부된 PDF 파일 키") String pdfFileKey) {

	// 전체 응답 객체 생성을 위한 정적 팩토리 메서드
	public static AdminApplicationPartQuestionResponse of(
			List<AdminPartQuestionResponse> questionsWithAnswers,
			String pdfFileUrl,
			String pdfFileKey) {
		return new AdminApplicationPartQuestionResponse(
				questionsWithAnswers, pdfFileUrl, pdfFileKey);
	}

	@Builder
	@Schema(description = "관리자용 개별 질문 및 답변 상세")
	public record AdminPartQuestionResponse(
			@Schema(description = "질문 순서") Integer sequence,
			@Schema(description = "질문 내용") String questionContent,
			@Schema(description = "작성된 답변 내용") String content,
			@Schema(description = "답변 바이트 수") int byteSize) {

		public static AdminPartQuestionResponse from(Question question, ApplicationAnswer answer) {
			String content = (answer != null ? answer.getContent() : null);

			return AdminPartQuestionResponse.builder()
					.sequence(question.getSequence())
					.questionContent(question.getContent())
					.content(content)
					.byteSize(content != null ? ByteManager.getByteSize(content) : 0)
					.build();
		}
	}
}
