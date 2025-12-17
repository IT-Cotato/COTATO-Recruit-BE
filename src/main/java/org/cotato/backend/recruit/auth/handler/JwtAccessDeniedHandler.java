package org.cotato.backend.recruit.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.common.error.ErrorCode;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** 인증된 사용자가 권한이 없는 리소스에 접근했을 때 처리하는 핸들러 */
/** SecurityConfig에서 권한이 필요한 api에 대해 hasRole("ADMIN") 등을 설정했을 때, 권한이 없는 사용자가 접근하면 이 핸들러가 호출된다 */
/** Config 예시 requestMatchers("/api/admin/**").hasRole("ADMIN")) */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException accessDeniedException)
			throws IOException {
		log.error("Access denied error: {}", accessDeniedException.getMessage());

		ErrorCode errorCode = ErrorCode.FORBIDDEN;
		response.setStatus(errorCode.getStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		ApiResponse<Void> apiResponse =
				ApiResponse.error(errorCode.getCode(), errorCode.getMessage());

		response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
	}
}
