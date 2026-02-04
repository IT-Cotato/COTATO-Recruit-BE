package org.cotato.backend.recruit.admin.dto.response.email;

import java.time.LocalDateTime;
import org.cotato.backend.recruit.domain.email.entity.EmailSendJob;
import org.cotato.backend.recruit.domain.email.entity.RecruitmentEmailTemplate;

public record RecruitmentEmailTemplateResponse(
		Long templateId,
		String content,
		boolean isSent,
		LocalDateTime sentAt,
		Long generationId,
		long subscriberCount,
		int successCount,
		int failCount) {

	public static RecruitmentEmailTemplateResponse of(
			RecruitmentEmailTemplate template, long subscriberCount, EmailSendJob job) {
		int successCount = job != null ? job.getSuccessCount() : 0;
		int failCount = job != null ? job.getFailCount() : 0;

		return new RecruitmentEmailTemplateResponse(
				template.getId(),
				template.getContent(),
				template.isSent(),
				template.getSentAt(),
				template.getGeneration().getId(),
				subscriberCount,
				successCount,
				failCount);
	}

	/** 모집 중인 기수가 없을 때의 응답 */
	public static RecruitmentEmailTemplateResponse empty() {
		return new RecruitmentEmailTemplateResponse(null, "", false, null, null, 0L, 0, 0);
	}
}
