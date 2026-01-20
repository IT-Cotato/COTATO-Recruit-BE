package org.cotato.backend.recruit.admin.dto.response.email;

import java.time.LocalDateTime;

public record RecruitmentEmailSendResponse(
		Long jobId, long totalCount, LocalDateTime sentAt, Long generationId) {

	public static RecruitmentEmailSendResponse of(
			Long jobId, long totalCount, LocalDateTime sentAt, Long generationId) {
		return new RecruitmentEmailSendResponse(jobId, totalCount, sentAt, generationId);
	}
}
