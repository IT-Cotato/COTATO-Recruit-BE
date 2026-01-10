package org.cotato.backend.recruit.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.cotato.backend.recruit.domain.application.enums.AnswerType;

@Schema(description = "질문 응답 요청")
public record AnswerRequest(
		@Schema(
						description = "응답하려는 질문의 ID",
						example = "1",
						requiredMode = Schema.RequiredMode.REQUIRED)
				@NotNull(message = "질문 ID는 필수입니다.")
				Long questionId,
		@Schema(
						description = "답변 타입 (CHECKBOX: 체크박스, TEXT: 텍스트, FILE: 파일)",
						example = "TEXT",
						allowableValues = {"CHECKBOX", "TEXT", "FILE"},
						requiredMode = Schema.RequiredMode.REQUIRED)
				@NotNull(message = "답변 타입은 필수입니다.")
				AnswerType answerType,
		@Schema(description = "체크박스 선택 여부 (answerType이 CHECKBOX일 때만 사용)", example = "true")
				Boolean isChecked,
		@Schema(
						description = "텍스트 답변 내용 (answerType이 TEXT일 때만 사용)",
						example = "저는 백엔드 개발자가 되고 싶습니다.")
				String content,
		@Schema(description = "파일 키 (answerType이 FILE일 때만 사용)", example = "files/abc123.pdf")
				String fileKey,
		@Schema(
						description = "파일 URL (answerType이 FILE일 때만 사용)",
						example = "https://s3.amazonaws.com/bucket/files/abc123.pdf")
				String fileUrl) {}
