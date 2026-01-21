package org.cotato.backend.recruit.excelReport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.cotato.backend.recruit.testsupport.ApiMetadata;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class TestReportManager implements TestWatcher {

	private static final List<TestResultDto> results =
			Collections.synchronizedList(new ArrayList<>());
	private static String webhookUrl;

	private static final ExcelReportWriter excelWriter = new ExcelReportWriter();
	private static final DiscordNotificationSender discordSender = new DiscordNotificationSender();

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(TestReportManager::finalizeReport));
	}

	@Override
	public void testSuccessful(ExtensionContext context) {
		collectResult(context, "PASS", "요청이 성공적으로 처리되었습니다.");
	}

	@Override
	public void testFailed(ExtensionContext context, Throwable cause) {
		String message = cause.getMessage() != null ? cause.getMessage() : "Unknown Error";
		collectResult(context, "FAIL", message);
	}

	private void collectResult(ExtensionContext context, String status, String message) {
		initWebhookUrl(context); // Webhook URL Lazy Loading

		String apiUrl =
				context.getTestClass()
						.map(clazz -> clazz.getAnnotation(ApiMetadata.class)) // 클래스 어노테이션만 확인
						.map(ApiMetadata::value)
						.orElse("-");

		results.add(new TestResultDto(apiUrl, context.getDisplayName(), status, message));
	}

	private void initWebhookUrl(ExtensionContext context) {
		if (webhookUrl == null) {
			try {
				Environment env = SpringExtension.getApplicationContext(context).getEnvironment();
				webhookUrl = env.getProperty("discord.webhook-url");
			} catch (Exception e) {
				// Spring Context 로딩 실패 시 무시
			}
		}
	}

	private static void finalizeReport() {
		if (results.isEmpty()) return;

		// 1. 엑셀 생성
		File excelFile = excelWriter.write(results);

		// 2. 통계 계산
		long passCount = results.stream().filter(r -> "PASS".equals(r.getStatus())).count();
		DiscordNotificationSender.TestStatistics stats =
				new DiscordNotificationSender.TestStatistics(
						results.size(), passCount, results.size() - passCount);

		// 3. 디스코드 전송
		discordSender.send(webhookUrl, excelFile, stats);
	}
}
