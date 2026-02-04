package org.cotato.backend.recruit.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "파트별 질문 지원서 답변 및 파일 저장 요청")
public record PartAnswerRequest(
		@Schema(description = "질문 응답 목록") List<AnswerRequest> answers,
		@Schema(description = "첨부된 PDF 파일 URL", example = "https://s3-bucket.com/files/resume.pdf")
				String pdfFileUrl,
		@Schema(description = "첨부된 PDF 파일 키", example = "files/resume.pdf") String pdfFileKey) {

	@Schema(description = "개별 질문 응답 요청")
	public record AnswerRequest(
			@Schema(
							description = "응답하려는 질문의 ID",
							example = "1",
							requiredMode = Schema.RequiredMode.REQUIRED)
					@NotNull(message = "질문 ID는 필수입니다.")
					Long questionId,
			@Schema(description = "텍스트 답변 내용", example = "저는 백엔드 개발자가 되고 싶습니다.") String content) {}
}
