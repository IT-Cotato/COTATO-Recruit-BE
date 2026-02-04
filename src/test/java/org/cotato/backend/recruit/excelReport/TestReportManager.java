package org.cotato.backend.recruit.excelReport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

	// [추가] 현재 실행 중인 스레드(테스트)의 로그를 임시 저장하는 공간
	private static final ThreadLocal<String> logHolder = new ThreadLocal<>();

	private static final ExcelReportWriter excelWriter = new ExcelReportWriter();
	private static final DiscordNotificationSender discordSender = new DiscordNotificationSender();

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(TestReportManager::finalizeReport));
	}

	// 테스트 코드에서 로그를 넣을 수 있도록 여는 메소드
	public static void setDetailLog(String log) {
		logHolder.set(log);
	}

	// 로그 초기화
	public static void clearLog() {
		logHolder.remove();
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
		initWebhookUrl(context);

		String testClassName = context.getTestClass().map(Class::getSimpleName).orElse("-");

		String apiUrl =
				context.getTestClass()
						.map(clazz -> clazz.getAnnotation(ApiMetadata.class))
						.map(ApiMetadata::value)
						.orElse("-");

		// ThreadLocal에서 로그 꺼내기 (없으면 빈 문자열)
		String details = logHolder.get();
		if (details == null) details = "";

		results.add(
				new TestResultDto(
						testClassName, apiUrl, context.getDisplayName(), status, message, details));

		// 다음 테스트를 위해 비우기
		clearLog();
	}

	private void initWebhookUrl(ExtensionContext context) {
		if (webhookUrl == null) {
			try {
				Environment env = SpringExtension.getApplicationContext(context).getEnvironment();
				webhookUrl = env.getProperty("discord.webhook-url");
			} catch (Exception e) {
				// Ignore
			}
		}
	}

	private static void finalizeReport() {
		if (results.isEmpty()) return;

		// 엑셀 파일 생성
		File excelFile = excelWriter.write(results);
		if (excelFile == null) return;

		// 통계 요약 파일 생성
		try (BufferedWriter writer =
				new BufferedWriter(new FileWriter("test_summary.properties"))) {
			long total = results.size();
			long pass = results.stream().filter(r -> "PASS".equals(r.getStatus())).count();
			long fail = total - pass;
			String endTime =
					LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

			// KEY=VALUE 형식으로 깔끔하게 작성
			writer.write("TEST_TOTAL=" + total);
			writer.newLine();
			writer.write("TEST_PASS=" + pass);
			writer.newLine();
			writer.write("TEST_FAIL=" + fail);
			writer.newLine();
			writer.write("TEST_END_TIME=" + endTime);
			writer.newLine();

			System.out.println("✅ [TestReportManager] 요약 파일 생성 완료: test_summary.properties");
		} catch (Exception e) {
			System.err.println("❌ 요약 파일 생성 실패: " + e.getMessage());
		}

		long passCount = results.stream().filter(r -> "PASS".equals(r.getStatus())).count();
		DiscordNotificationSender.TestStatistics stats =
				new DiscordNotificationSender.TestStatistics(
						results.size(), passCount, results.size() - passCount);

		discordSender.send(webhookUrl, excelFile, stats);
	}
}
