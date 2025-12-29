package org.cotato.backend.recruit.admin.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApplicationAdminErrorCode {
	GENERATION_NOT_FOUND(HttpStatus.NOT_FOUND, "GEN-001", "해당 기수를 찾을 수 없습니다."),

	APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "APP-001", "해당 지원서를 찾을 수 없습니다."),
	INVALID_PART_TYPE(HttpStatus.BAD_REQUEST, "APP-002", "유효하지 않은 파트 타입입니다."),
	INVALID_PASS_STATUS(HttpStatus.BAD_REQUEST, "APP-003", "유효하지 않은 합격 여부입니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
