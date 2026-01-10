package org.cotato.backend.recruit.admin.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AdminErrorCode {

	// Email Template
	EMAIL_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "EM001", "이메일 템플릿을 찾을 수 없습니다."),
	EMAIL_ALREADY_SENT(HttpStatus.BAD_REQUEST, "EM002", "이미 전송된 이메일입니다."),
	EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EM003", "이메일 전송에 실패했습니다."),

	// Recruitment
	RECRUITMENT_NOT_ACTIVE(HttpStatus.FORBIDDEN, "RE001", "현재 모집이 활성화되어 있지 않습니다."),

	// Generation
	GENERATION_NOT_FOUND(HttpStatus.NOT_FOUND, "GE001", "모집 중인 기수를 찾을 수 없습니다."),
	NO_ACTIVE_GENERATION(HttpStatus.BAD_REQUEST, "GE002", "현재 모집 중인 기수가 없습니다."),

	// Application
	APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "AP001", "지원서를 찾을 수 없습니다."),

	// Part Type
	PART_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "PT001", "해당 파트 타입을 찾을 수 없습니다."),

	// Pass Status
	PASS_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "PS001", "해당 패스 상태를 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
