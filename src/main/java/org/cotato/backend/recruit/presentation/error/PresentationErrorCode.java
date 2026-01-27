package org.cotato.backend.recruit.presentation.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PresentationErrorCode {

	// Recruitment
	RECRUITMENT_NOT_ACTIVE(HttpStatus.FORBIDDEN, "RE001", "현재 모집이 활성화되어 있지 않습니다."),
	RECRUITMENT_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "RE002", "모집 정보를 찾을 수 없습니다."),

	// Application
	APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "AP001", "지원서를 찾을 수 없습니다."),
	ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "AP002", "이미 제출된 지원서입니다."),
	APPLICATION_FORBIDDEN(HttpStatus.FORBIDDEN, "AP003", "해당 지원서에 접근할 권한이 없습니다."),
	PART_TYPE_NOT_SELECTED(HttpStatus.BAD_REQUEST, "AP004", "지원 파트가 선택되지 않았습니다."),
	RECRUITMENT_PERIOD_ENDED(HttpStatus.BAD_REQUEST, "AP005", "지원 기간이 종료되었습니다."),
	RECRUITMENT_PERIOD_NOT_STARTED(HttpStatus.BAD_REQUEST, "AP007", "아직 지원 기간이 시작되지 않았습니다."),
	PARALLEL_ACTIVITIES_TOO_LONG(HttpStatus.BAD_REQUEST, "AP006", "병행 활동은 600자를 초과할 수 없습니다."),
	REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "AP008", "필수 입력 항목이 누락되었습니다."),
	INVALID_JSON_FORMAT(HttpStatus.INTERNAL_SERVER_ERROR, "AP009", "JSON 형식이 올바르지 않습니다."),

	// Generation
	GENERATION_NOT_FOUND(HttpStatus.NOT_FOUND, "GE001", "모집 중인 기수를 찾을 수 없습니다."),

	// Question
	QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QU001", "질문을 찾을 수 없습니다."),

	// Answer
	ANSWER_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "AN001", "질문의 답변 타입과 제출한 답변 타입이 일치하지 않습니다."),
	ANSWER_CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "AN002", "답변 내용이 너무 깁니다."),

	// Subscription
	ALREADY_SUBSCRIBED(HttpStatus.BAD_REQUEST, "SU001", "이미 구독 신청된 이메일입니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
