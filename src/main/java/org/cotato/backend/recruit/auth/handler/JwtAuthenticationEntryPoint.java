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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/** 인증되지 않은 사용자가 보호된 리소스에 접근했을 때 처리하는 핸들러 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException)
			throws IOException {
		log.error("Unauthorized error: {}", authException.getMessage());

		ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
		response.setStatus(errorCode.getStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		ApiResponse<Void> apiResponse =
				ApiResponse.error(errorCode.getCode(), errorCode.getMessage());

		response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
	}
}
