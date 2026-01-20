package org.cotato.backend.recruit.common.config;

import io.sentry.IHub;
import io.sentry.Sentry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentryConfig {

	@Value("${sentry.dsn}")
	private String dsn;

	// 1. 초기화는 @PostConstruct에서 딱 한 번 수행
	@PostConstruct
	public void init() {
		Sentry.init(
				options -> {
					options.setDsn(dsn);
					options.setDebug(false);
					options.setTracesSampleRate(1.0);
				});
	}

	// 2. IHub 빈은 이미 초기화된 Sentry에서 가져와서 등록
	@Bean
	public IHub sentryHub() {
		return Sentry.getCurrentHub();
	}
}
