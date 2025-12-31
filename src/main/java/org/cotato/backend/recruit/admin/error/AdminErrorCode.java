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

	// Generation
	NO_ACTIVE_GENERATION(HttpStatus.BAD_REQUEST, "GE004", "현재 모집 중인 기수가 없습니다."),
	GENERATION_NOT_FOUND(HttpStatus.NOT_FOUND, "GE005", "해당 기수를 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
