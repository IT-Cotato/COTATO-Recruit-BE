package org.cotato.backend.recruit.excelReport;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DiscordNotificationSender {

	public void send(String webhookUrl, File file, TestStatistics stats) {
		if (webhookUrl == null || webhookUrl.isBlank() || webhookUrl.contains("YOUR_WEBHOOK_URL")) {
			System.out.println("⚠️ [Discord] Webhook URL이 설정되지 않아 전송을 건너뜁니다.");
			return;
		}

		try {
			if (!file.exists()) return;

			String boundary = "---" + UUID.randomUUID();
			String message =
					String.format(
							"📢 **통합 테스트 결과 리포트**\n- 전체: %d건\n- 성공: %d건\n- 실패: %d건",
							stats.getTotal(), stats.getPass(), stats.getFail());

			// Multipart Body 구성
			List<byte[]> multipartBody = new ArrayList<>();
			addFormField(multipartBody, boundary, "content", message);
			addFilePart(multipartBody, boundary, "file", file);
			multipartBody.add(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

			byte[] finalBody = concatenateBytes(multipartBody);

			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request =
					HttpRequest.newBuilder()
							.uri(URI.create(webhookUrl))
							.header("Content-Type", "multipart/form-data; boundary=" + boundary)
							.POST(HttpRequest.BodyPublishers.ofByteArray(finalBody))
							.timeout(Duration.ofSeconds(10))
							.build();

			HttpResponse<String> response =
					client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() >= 200 && response.statusCode() < 300) {
				System.out.println("🚀 [Discord] 리포트 전송 성공!");
			} else {
				System.err.println("❌ [Discord] 전송 실패: " + response.statusCode());
			}

		} catch (Exception e) {
			System.err.println("❌ [Discord] 에러 발생: " + e.getMessage());
		}
	}

	private void addFormField(List<byte[]> body, String boundary, String name, String value) {
		body.add(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
		body.add(
				("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n")
						.getBytes(StandardCharsets.UTF_8));
		body.add((value + "\r\n").getBytes(StandardCharsets.UTF_8));
	}

	private void addFilePart(List<byte[]> body, String boundary, String name, File file)
			throws Exception {
		body.add(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
		body.add(
				("Content-Disposition: form-data; name=\""
								+ name
								+ "\"; filename=\""
								+ file.getName()
								+ "\"\r\n")
						.getBytes(StandardCharsets.UTF_8));
		body.add(
				"Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\r\n\r\n"
						.getBytes(StandardCharsets.UTF_8));
		body.add(Files.readAllBytes(file.toPath()));
	}

	private byte[] concatenateBytes(List<byte[]> parts) {
		int totalSize = parts.stream().mapToInt(b -> b.length).sum();
		byte[] result = new byte[totalSize];
		int currentPos = 0;
		for (byte[] part : parts) {
			System.arraycopy(part, 0, result, currentPos, part.length);
			currentPos += part.length;
		}
		return result;
	}

	// 통계 정보를 담기 위한 간단한 DTO
	@lombok.Getter
	@lombok.AllArgsConstructor
	public static class TestStatistics {
		private long total;
		private long pass;
		private long fail;
	}
}
