package org.cotato.backend.recruit.presentation.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApplicationErrorCode {

	// Application
	APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "AP001", "지원서를 찾을 수 없습니다."),
	ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "AP002", "이미 제출된 지원서입니다."),
	APPLICATION_FORBIDDEN(HttpStatus.FORBIDDEN, "AP003", "해당 지원서에 접근할 권한이 없습니다."),

	// Generation
	GENERATION_NOT_FOUND(HttpStatus.NOT_FOUND, "GE001", "모집 중인 기수를 찾을 수 없습니다."),

	// Question
	QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QU001", "질문을 찾을 수 없습니다."),

	// Answer
	ANSWER_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "AN001", "질문의 답변 타입과 제출한 답변 타입이 일치하지 않습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
