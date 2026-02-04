package org.cotato.backend.recruit.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// Common
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 내부 오류가 발생했습니다."),
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C002", "잘못된 입력값입니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C003", "허용되지 않는 HTTP 메서드입니다."),
	INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "잘못된 타입입니다."),
	HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "접근이 거부되었습니다."),
	MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "C006", "필수 파라미터가 누락되었습니다."),

	// Authentication & Authorization
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다."),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "만료된 토큰입니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "A004", "권한이 없습니다."),
	REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A005", "Refresh Token을 찾을 수 없습니다."),
	REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "A006", "Refresh Token이 일치하지 않습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "A007", "사용자를 찾을 수 없습니다."),
	OAUTH2_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A008", "OAuth2 인증에 실패했습니다."),

	// File Upload
	FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F001", "파일 업로드에 실패했습니다."),
	INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "F002", "허용되지 않는 파일 형식입니다."),
	FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "F003", "파일 크기가 제한을 초과했습니다."),
	EMPTY_FILE(HttpStatus.BAD_REQUEST, "F004", "빈 파일은 업로드할 수 없습니다."),
	PRE_SIGNED_URL_GENERATION_FAILED(
			HttpStatus.INTERNAL_SERVER_ERROR, "F005", "Pre-signed URL 생성에 실패했습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
