package org.cotato.backend.recruit.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.auth.dto.CustomUserDetails;
import org.cotato.backend.recruit.auth.jwt.JwtTokenProvider;
import org.cotato.backend.recruit.common.error.ErrorCode;
import org.cotato.backend.recruit.common.exception.GlobalException;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.cotato.backend.recruit.domain.user.entity.User;
import org.cotato.backend.recruit.domain.user.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
/** JWT 인증 필터, 모든 요청에 대해 JWT 토큰을 검사하고, 유효한 경우 인증 정보를 SecurityContext에 설정한다 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION_HEADER = "Authorization"; // HTTP 헤더 이름
	private static final String BEARER_PREFIX = "Bearer "; // 접두사

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String jwt = extractJwtFromRequest(request);

			if (StringUtils.hasText(jwt)) {
				// JWT 유효성 검증
				if (!jwtTokenProvider.validateToken(jwt)) {
					log.warn("Invalid JWT token");
					handleException(response, ErrorCode.INVALID_TOKEN);
					return;
				}

				// JWT에서 사용자 ID 추출
				String userId = jwtTokenProvider.getUserIdFromToken(jwt);

				// 사용자 조회
				User user =
						userRepository
								.findById(Long.parseLong(userId))
								.orElseThrow(
										() -> {
											log.error("User not found with id: {}", userId);
											return new GlobalException(ErrorCode.USER_NOT_FOUND);
										});

				// CustomUserDetails 생성
				CustomUserDetails userDetails = CustomUserDetails.from(user);

				// Spring Security 인증 객체 생성
				UsernamePasswordAuthenticationToken authentication =
						new UsernamePasswordAuthenticationToken(
								userDetails, null, userDetails.getAuthorities());

				// 인증 세부 정보 설정, 요청 정보 포함, 예: IP 주소, 세션 ID 등
				authentication.setDetails(
						new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContextHolder.getContext().setAuthentication(authentication);
				log.debug("Set authentication for user: {}", userId);
			}
		} catch (GlobalException ex) {
			log.error("GlobalException in JWT filter: {}", ex.getMessage());
			handleException(response, ex.getErrorCode());
			return;
		} catch (Exception ex) {
			log.error("Could not set user authentication in security context", ex);
			handleException(response, ErrorCode.INVALID_TOKEN);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private String extractJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
			return bearerToken.substring(BEARER_PREFIX.length());
		}
		return null;
	}

	private void handleException(HttpServletResponse response, ErrorCode errorCode)
			throws IOException {
		response.setStatus(errorCode.getStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		ApiResponse<Void> apiResponse =
				ApiResponse.error(errorCode.getCode(), errorCode.getMessage());

		response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
	}
}
