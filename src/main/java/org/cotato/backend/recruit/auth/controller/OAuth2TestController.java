package org.cotato.backend.recruit.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "OAuth2 테스트 API", description = "프론트엔드 없이 OAuth2 로그인을 테스트할 수 있는 API (개발용)")
@RestController
@RequestMapping("/api/test/oauth2")
@RequiredArgsConstructor
public class OAuth2TestController {

	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private String clientId;

	@Value("${server.port:8080}")
	private String serverPort;

	private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";

	/** Google 로그인 페이지로 리다이렉트 브라우저에서 http://localhost:8080/api/test/oauth2/google/login 접속 */
	@Operation(
			summary = "[테스트] Google 로그인",
			description = "브라우저에서 이 URL에 접속하면 Google 로그인 페이지로 이동합니다.")
	@GetMapping("/google/login")
	public void loginWithGoogle(HttpServletResponse response) throws IOException {
		String redirectUri = "http://localhost:" + serverPort + "/api/test/oauth2/google/callback";

		String googleAuthUrl =
				GOOGLE_AUTH_URL
						+ "?client_id="
						+ clientId
						+ "&redirect_uri="
						+ URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
						+ "&response_type=code"
						+ "&scope="
						+ URLEncoder.encode("openid profile email", StandardCharsets.UTF_8);

		log.info("Redirecting to Google OAuth - redirectUri: {}", redirectUri);
		response.sendRedirect(googleAuthUrl);
	}

	/** Google에서 Authorization Code를 받아서 화면에 표시 Google 로그인 후 자동으로 이 엔드포인트로 리다이렉트됨 */
	@Operation(
			summary = "[테스트] Google 콜백",
			description = "Google에서 Authorization Code를 받아서 화면에 표시합니다.")
	@GetMapping("/google/callback")
	public void handleGoogleCallback(
			@Parameter(description = "Google Authorization Code") @RequestParam String code,
			HttpServletResponse response)
			throws IOException {

		String redirectUri = "http://localhost:" + serverPort + "/api/test/oauth2/google/callback";

		// HTML 페이지로 Authorization Code 표시
		String html = buildCallbackHtml(code, redirectUri);
		response.setContentType(MediaType.TEXT_HTML_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(html);
	}

	private String buildCallbackHtml(String code, String redirectUri) {
		return """
				<!DOCTYPE html>
				<html>
				<head>
					<meta charset="UTF-8">
					<title>Authorization Code</title>
				</head>
				<body>
					<h3>Authorization Code 받기 성공</h3>
					<p><strong>code:</strong> %s</p>
					<p><strong>redirectUri:</strong> %s</p>
					<br>
					<a href="/api/test/oauth2/google/login">다시 테스트하기</a>
				</body>
				</html>
				"""
				.formatted(code, redirectUri);
	}
}
