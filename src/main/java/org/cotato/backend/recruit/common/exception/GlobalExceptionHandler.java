package org.cotato.backend.recruit.common.exception;

import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.admin.error.AdminErrorCode;
import org.cotato.backend.recruit.admin.error.ApplicationAdminErrorCode;
import org.cotato.backend.recruit.admin.exception.AdminException;
import org.cotato.backend.recruit.admin.exception.ApplicationAdminException;
import org.cotato.backend.recruit.common.error.ErrorCode;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.cotato.backend.recruit.presentation.error.ApplicationErrorCode;
import org.cotato.backend.recruit.presentation.exception.ApplicationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/** ApplicationException 처리 */
	@ExceptionHandler(ApplicationException.class)
	protected ApiResponse<Void> handleApplicationException(
			ApplicationException e, HttpServletResponse response) {
		log.error("ApplicationException: {}", e.getMessage());
		ApplicationErrorCode errorCode = e.getErrorCode();
		response.setStatus(errorCode.getStatus().value());
		return ApiResponse.error(errorCode.getCode(), e.getMessage());
	}

	/** ApplicationAdminException 처리 (HEAD) */
	@ExceptionHandler(ApplicationAdminException.class)
	protected ApiResponse<Void> handleApplicationAdminException(
			ApplicationAdminException e, HttpServletResponse response) {
		log.error("ApplicationAdminException: {}", e.getMessage());
		ApplicationAdminErrorCode errorCode = e.getErrorCode();
		response.setStatus(errorCode.getStatus().value());
		return ApiResponse.error(errorCode.getCode(), e.getMessage());
	}

	/** EmailAdminException/AdminException 처리 (Remote) */
	@ExceptionHandler(AdminException.class)
	protected ApiResponse<Void> handleEmailAdminException(
			AdminException e, HttpServletResponse response) {
		log.error("EmailAdminException: {}", e.getMessage(), e);
		AdminErrorCode errorCode = e.getErrorCode();
		response.setStatus(errorCode.getStatus().value());
		return ApiResponse.error(errorCode.getCode(), e.getMessage());
	}

	/** IllegalArgumentException 처리 (HEAD) */
	@ExceptionHandler(IllegalArgumentException.class)
	protected ApiResponse<Void> handleIllegalArgumentException(
			IllegalArgumentException e, HttpServletResponse response) {
		log.error("IllegalArgumentException: {}", e.getMessage());
		ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
		response.setStatus(errorCode.getStatus().value());
		return ApiResponse.error(errorCode.getCode(), e.getMessage());
	}

	/** GlobalException 처리 */
	@ExceptionHandler(GlobalException.class)
	protected ApiResponse<Void> handleGlobalException(
			GlobalException e, HttpServletResponse response) {
		log.error("GlobalException: {}", e.getMessage(), e);
		ErrorCode errorCode = e.getErrorCode();
		response.setStatus(errorCode.getStatus().value());
		return ApiResponse.error(errorCode.getCode(), e.getMessage());
	}

	/**
	 * @Valid 검증 실패 시 발생 (RequestBody)
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ApiResponse<Map<String, String>> handleMethodArgumentNotValidException(
			MethodArgumentNotValidException e, HttpServletResponse response) {
		log.error("MethodArgumentNotValidException: {}", e.getMessage(), e);

		Map<String, String> errors = new HashMap<>();
		e.getBindingResult()
				.getAllErrors()
				.forEach(
						error -> {
							String fieldName = ((FieldError) error).getField();
							String errorMessage = error.getDefaultMessage();
							errors.put(fieldName, errorMessage);
						});

		response.setStatus(ErrorCode.INVALID_INPUT_VALUE.getStatus().value());
		return ApiResponse.error(
				ErrorCode.INVALID_INPUT_VALUE.getCode(),
				ErrorCode.INVALID_INPUT_VALUE.getMessage(),
				errors);
	}

	/** 파라미터 타입 불일치 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	protected ApiResponse<Void> handleMethodArgumentTypeMismatchException(
			MethodArgumentTypeMismatchException e, HttpServletResponse response) {
		log.error("MethodArgumentTypeMismatchException: {}", e.getMessage(), e);
		response.setStatus(ErrorCode.INVALID_TYPE_VALUE.getStatus().value());
		return ApiResponse.error(
				ErrorCode.INVALID_TYPE_VALUE.getCode(), ErrorCode.INVALID_TYPE_VALUE.getMessage());
	}

	/** 필수 파라미터 누락 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	protected ApiResponse<Void> handleMissingServletRequestParameterException(
			MissingServletRequestParameterException e, HttpServletResponse response) {
		log.error("MissingServletRequestParameterException: {}", e.getMessage(), e);
		response.setStatus(ErrorCode.MISSING_REQUEST_PARAMETER.getStatus().value());
		return ApiResponse.error(
				ErrorCode.MISSING_REQUEST_PARAMETER.getCode(),
				ErrorCode.MISSING_REQUEST_PARAMETER.getMessage());
	}

	/** 지원하지 않는 HTTP 메서드 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	protected ApiResponse<Void> handleHttpRequestMethodNotSupportedException(
			HttpRequestMethodNotSupportedException e, HttpServletResponse response) {
		log.error("HttpRequestMethodNotSupportedException: {}", e.getMessage(), e);
		response.setStatus(ErrorCode.METHOD_NOT_ALLOWED.getStatus().value());
		return ApiResponse.error(
				ErrorCode.METHOD_NOT_ALLOWED.getCode(), ErrorCode.METHOD_NOT_ALLOWED.getMessage());
	}

	/** 그 외 모든 예외 처리 */
	@ExceptionHandler(Exception.class)
	protected ApiResponse<Void> handleException(Exception e, HttpServletResponse response) {
		log.error("Exception: {}", e.getMessage(), e);
		response.setStatus(ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value());
		return ApiResponse.error(
				ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
				ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
	}
}