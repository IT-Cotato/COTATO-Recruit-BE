package org.cotato.backend.recruit.common.aop;

import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.cotato.backend.recruit.auth.dto.CustomUserDetails;
import org.cotato.backend.recruit.common.annotation.MonitorFailure;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FailureMonitorAspect {

	@Around("@annotation(org.cotato.backend.recruit.common.annotation.MonitorFailure) || "
			+ "@within(org.cotato.backend.recruit.common.annotation.MonitorFailure)")
	public Object handleFailure(ProceedingJoinPoint joinPoint) throws Throwable {
		try {
			return joinPoint.proceed();
		} catch (Exception e) {
			// 1. 어노테이션 정보 가져오기
			MonitorFailure monitorFailure = getAnnotation(joinPoint);

			// 2. 로그 이름 결정 (어노테이션 값이 없으면 "메서드이름"으로 자동 지정)
			String logName = monitorFailure.logName() != null ? monitorFailure.logName()
					: joinPoint.getSignature().getName();

			// 파라미터 수집 로직 호출
			Map<String, Object> requestParams = getMethodParams(joinPoint);

			// Sentry 전송
			Sentry.configureScope(
					scope -> {
						scope.setTag("alert_needed", String.valueOf(monitorFailure.alertNeeded()));
						scope.setTag("api_name", logName);
						scope.setTag(
								"controller",
								joinPoint.getSignature().getDeclaringType().getSimpleName());
						scope.setContexts("Request Params", requestParams);

						Sentry.captureException(e);
					});

			throw e;
		}
	}

	/** 메서드 파라미터 이름과 값을 추출하여 Map으로 반환 */
	private Map<String, Object> getMethodParams(ProceedingJoinPoint joinPoint) {
		Map<String, Object> params = new HashMap<>();

		try {
			Object[] args = joinPoint.getArgs(); // 파라미터 값들
			String[] argNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames(); // 파라미터 이름들

			for (int i = 0; i < args.length; i++) {
				Object arg = args[i];
				String name = argNames[i];

				// 사용자 ID만 추출
				if (arg instanceof CustomUserDetails) {
					params.put("auth_user_id", ((CustomUserDetails) arg).getUserId());
					continue; // 다음 루프로 건너뜀 (원본 객체 저장 X)
				}

				// 불필요하거나 직렬화 불가능한 객체는 제외
				if (arg instanceof HttpServletRequest
						|| arg instanceof HttpServletResponse
						|| arg instanceof BindingResult
						|| name.equals("userDetails")) { // UserDetails는 필요하면 ID만 넣던가 제외
					continue;
				}

				params.put(name, arg);
			}
		} catch (Exception e) {
			log.warn("파라미터 수집 중 에러 (무시됨): {}", e.getMessage());
			params.put("error", "Failed to parsing params");
		}

		return params;
	}

	private MonitorFailure getAnnotation(ProceedingJoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		MonitorFailure annotation = method.getAnnotation(MonitorFailure.class);
		if (annotation == null) {
			annotation = joinPoint.getTarget().getClass().getAnnotation(MonitorFailure.class);
		}
		return annotation;
	}
}
