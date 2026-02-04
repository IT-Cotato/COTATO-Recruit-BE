package org.cotato.backend.recruit.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.admin.error.AdminErrorCode;
import org.cotato.backend.recruit.admin.exception.AdminException;
import org.cotato.backend.recruit.common.error.ErrorCode;
import org.cotato.backend.recruit.common.response.ApiResponse;

import org.cotato.backend.recruit.presentation.error.PresentationErrorCode;
import org.cotato.backend.recruit.presentation.exception.PresentationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private final ExceptionAnalysisLogger exceptionAnalysisLogger;

	/** PresentationException 처리 */
	@ExceptionHandler(PresentationException.class)
	protected ApiResponse<Void> handlePresentationException(
			PresentationException e, HttpServletRequest request, HttpServletResponse response) {
		exceptionAnalysisLogger.logAiAnalysisData(e, request);
		PresentationErrorCode errorCode = e.getErrorCode();
		response.setStatus(errorCode.getStatus().value());
		return ApiResponse.error(errorCode.getCode(), e.getMessage());
	}

	/** AdminException 처리 */
	@ExceptionHandler(AdminException.class)
	protected ApiResponse<Void> handleAdminException(
			AdminException e, HttpServletRequest request, HttpServletResponse response) {
		exceptionAnalysisLogger.logAiAnalysisData(e, request);
		AdminErrorCode errorCode = e.getErrorCode();
		response.setStatus(errorCode.getStatus().value());
		return ApiResponse.error(errorCode.getCode(), e.getMessage());
	}

	/** IllegalArgumentException 처리 (HEAD) */
	@ExceptionHandler(IllegalArgumentException.class)
	protected ApiResponse<Void> handleIllegalArgumentException(
			IllegalArgumentException e, HttpServletRequest request, HttpServletResponse response) {
		exceptionAnalysisLogger.logAiAnalysisData(e, request);
		ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
		response.setStatus(errorCode.getStatus().value());
		return ApiResponse.error(errorCode.getCode(), e.getMessage());
	}

	/** GlobalException 처리 */
	@ExceptionHandler(GlobalException.class)
	protected ApiResponse<Void> handleGlobalException(
			GlobalException e, HttpServletRequest request, HttpServletResponse response) {
		exceptionAnalysisLogger.logAiAnalysisData(e, request);
		ErrorCode errorCode = e.getErrorCode();
		response.setStatus(errorCode.getStatus().value());
		return ApiResponse.error(errorCode.getCode(), e.getMessage());
	}

	/**
	 * @Valid 검증 실패 시 발생 (RequestBody)
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ApiResponse<Map<String, String>> handleMethodArgumentNotValidException(
			MethodArgumentNotValidException e, HttpServletRequest request, HttpServletResponse response) {
		exceptionAnalysisLogger.logAiAnalysisData(e, request);

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
			MethodArgumentTypeMismatchException e, HttpServletRequest request, HttpServletResponse response) {
		exceptionAnalysisLogger.logAiAnalysisData(e, request);
		response.setStatus(ErrorCode.INVALID_TYPE_VALUE.getStatus().value());
		return ApiResponse.error(
				ErrorCode.INVALID_TYPE_VALUE.getCode(), ErrorCode.INVALID_TYPE_VALUE.getMessage());
	}

	/** 필수 파라미터 누락 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	protected ApiResponse<Void> handleMissingServletRequestParameterException(
			MissingServletRequestParameterException e, HttpServletRequest request, HttpServletResponse response) {
		exceptionAnalysisLogger.logAiAnalysisData(e, request);
		response.setStatus(ErrorCode.MISSING_REQUEST_PARAMETER.getStatus().value());
		return ApiResponse.error(
				ErrorCode.MISSING_REQUEST_PARAMETER.getCode(),
				ErrorCode.MISSING_REQUEST_PARAMETER.getMessage());
	}

	/** 지원하지 않는 HTTP 메서드 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	protected ApiResponse<Void> handleHttpRequestMethodNotSupportedException(
			HttpRequestMethodNotSupportedException e, HttpServletRequest request, HttpServletResponse response) {
		exceptionAnalysisLogger.logAiAnalysisData(e, request);
		response.setStatus(ErrorCode.METHOD_NOT_ALLOWED.getStatus().value());
		return ApiResponse.error(
				ErrorCode.METHOD_NOT_ALLOWED.getCode(), ErrorCode.METHOD_NOT_ALLOWED.getMessage());
	}

	/** 그 외 모든 예외 처리 */
	@ExceptionHandler(Exception.class)
	protected ApiResponse<Void> handleException(Exception e, HttpServletRequest request, HttpServletResponse response) {
		exceptionAnalysisLogger.logAiAnalysisData(e, request);
		response.setStatus(ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value());
		return ApiResponse.error(
				ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
				ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
	}
}
