package org.cotato.backend.recruit.common.config;

import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.auth.filter.JwtAuthenticationFilter;
import org.cotato.backend.recruit.auth.handler.JwtAccessDeniedHandler;
import org.cotato.backend.recruit.auth.handler.JwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Spring Security 설정 클래스 (JWT 기반 인증을 설정한다.) */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// CSRF 비활성화 (JWT 사용으로 불필요)
				.csrf(AbstractHttpConfigurer::disable)
				// CORS 설정 (WebConfig에서 설정)
				.cors(cors -> {
				})
				// Form 로그인 비활성화
				.formLogin(AbstractHttpConfigurer::disable)
				// HTTP Basic 인증 비활성화
				.httpBasic(AbstractHttpConfigurer::disable)
				// 세션 관리: STATELESS (JWT 사용)
				.sessionManagement(
						session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// 예외 처리 설정
				.exceptionHandling(
						exception -> exception
								.authenticationEntryPoint(
										jwtAuthenticationEntryPoint) // 인증 실패 처리
								.accessDeniedHandler(jwtAccessDeniedHandler)) // 인가 실패 처리
				// 요청별 인가 설정
				.authorizeHttpRequests(
						authorize -> authorize
								// 공개 엔드포인트
								.requestMatchers(
										"/",
										"/api-docs/**",
										"/swagger-ui/**",
										"/swagger-ui.html",
										"/actuator/**",
										"/api/auth/login/**",
										"/api/auth/refresh",
										"/api/test/oauth2/**",
										"/api/faq/**",
										"/api/recruitment/**",
										"/backend/**")
								.permitAll()
								// admin api는 STAFF 역할만 접근 가능
								.requestMatchers("/api/admin/**")
								.hasRole("STAFF")
								// 그 외 모든 요청은 인증 필요
								.anyRequest()
								.authenticated())
				// JWT 인증 필터 추가 (UsernamePasswordAuthenticationFilter 이전에 실행)

				.addFilterBefore(
						jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
