package org.cotato.backend.recruit.admin.dto.response.email;

import java.time.LocalDateTime;
import org.cotato.backend.recruit.domain.email.entity.EmailSendJob;

public record EmailJobStatusResponse(
		Long jobId,
		boolean isCompleted,
		int totalCount,
		int successCount,
		int failCount,
		LocalDateTime createdAt,
		LocalDateTime completedAt,
		Long generationId) {

	public static EmailJobStatusResponse from(EmailSendJob job) {
		return new EmailJobStatusResponse(
				job.getId(),
				job.isCompleted(),
				job.getTotalCount(),
				job.getSuccessCount(),
				job.getFailCount(),
				job.getCreatedAt(),
				job.getCompletedAt(),
				job.getGeneration().getId());
	}
}
