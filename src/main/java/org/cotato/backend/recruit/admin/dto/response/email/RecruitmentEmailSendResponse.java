package org.cotato.backend.recruit.admin.dto.response.email;

import java.time.LocalDateTime;

public record RecruitmentEmailSendResponse(
		long successCount, long failCount, LocalDateTime sentAt, Long generationId) {

	public static RecruitmentEmailSendResponse of(
			long successCount, long failCount, LocalDateTime sentAt, Long generationId) {
		return new RecruitmentEmailSendResponse(successCount, failCount, sentAt, generationId);
	}
}
