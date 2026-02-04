package org.cotato.backend.recruit.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * Exception 분석을 위한 AI 로깅 서비스
 *
 * <p>발생한 예외를 AI가 분석할 수 있도록 구조화된 JSON 로그를 생성합니다.
 *
 * <ul>
 *   <li>Error Summary: 예외 타입, 메시지, 근본 원인
 *   <li>Request Context: HTTP 메서드, URL, 사용자 ID, 요청 페이로드
 *   <li>Code Context: 프로젝트 내 예외 발생 위치 (파일, 클래스, 라인, 메서드)
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExceptionAnalysisLogger {

	private final ObjectMapper objectMapper;

	/**
	 * AI에게 보낼 문맥 데이터 생성 및 로그 출력
	 *
	 * @param e 발생한 예외
	 * @param request HTTP 요청 객체
	 */
	public void logAiAnalysisData(Exception e, HttpServletRequest request) {
		try {
			// 1. AI에게 보낼 문맥 데이터 생성
			Map<String, Object> aiContext = new HashMap<>();

			// [Section 1] Error Summary (에러 요약)
			Map<String, String> errorSummary = new HashMap<>();
			errorSummary.put("exception_type", e.getClass().getName());
			errorSummary.put("message", e.getMessage());
			errorSummary.put("root_cause", getRootCause(e).toString()); // 가장 깊은 원인
			aiContext.put("error_summary", errorSummary);

			// [Section 2] Request Context (요청 정보)
			Map<String, Object> requestContext = new HashMap<>();
			requestContext.put("method", request.getMethod());
			requestContext.put("url", request.getRequestURI());
			requestContext.put("user_id", getUserId()); // 현재 로그인한 유저
			requestContext.put("payload", getRequestBody(request)); // 사용자가 보낸 데이터
			aiContext.put("request_context", requestContext);

			// [Section 3] Code Context (우리 코드 위치 찾기)
			StackTraceElement targetTrace = findMyCodeTrace(e);
			if (targetTrace != null) {
				Map<String, Object> codeContext = new HashMap<>();
				codeContext.put("file", targetTrace.getFileName());
				codeContext.put("class", targetTrace.getClassName());
				codeContext.put("line", targetTrace.getLineNumber());
				codeContext.put("method", targetTrace.getMethodName());
				aiContext.put("code_context", codeContext);
			}

			// 2. JSON으로 변환하여 로그 출력 (이 로그를 긁어서 AI에게 주면 됨)
			String jsonLog =
					objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(aiContext);
			log.error("🚨 [AI Analysis Data] \n{}", jsonLog);

		} catch (Exception jsonError) {
			log.error("JSON 변환 실패: {}", jsonError.getMessage());
		}
	}

	// --- Helper Methods ---

	/** 근본 원인(Root Cause) 찾기 */
	private Throwable getRootCause(Exception e) {
		Throwable cause = e;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		return cause;
	}

	/** 내 프로젝트 패키지(org.cotato)에서 발생한 에러 위치 찾기 */
	private StackTraceElement findMyCodeTrace(Exception e) {
		for (StackTraceElement element : e.getStackTrace()) {
			if (element.getClassName().startsWith("org.cotato")) {
				return element;
			}
		}
		return e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	}

	/** Request Body 읽어오기 (ContentCachingRequestWrapper 필요) */
	private String getRequestBody(HttpServletRequest request) {
		ContentCachingRequestWrapper wrapper = null;
		if (request instanceof ContentCachingRequestWrapper) {
			wrapper = (ContentCachingRequestWrapper) request;
		}

		if (wrapper != null) {
			byte[] buf = wrapper.getContentAsByteArray();
			if (buf.length > 0) {
				try {
					return new String(buf, 0, buf.length, StandardCharsets.UTF_8);
				} catch (Exception e) {
					return "Body parsing error";
				}
			}
		}
		return "Empty or Not Readable";
	}

	/** SecurityContext에서 유저 ID 꺼내기 */
	private String getUserId() {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null && auth.isAuthenticated()) {
				return auth.getName(); // 보통 여기에 ID나 email이 들어있음
			}
		} catch (Exception e) {
			return "Anonymous";
		}
		return "Anonymous";
	}
}
