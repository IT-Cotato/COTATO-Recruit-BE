package org.cotato.backend.recruit.admin.dto.response.email;

import java.time.LocalDateTime;
import org.cotato.backend.recruit.domain.email.entity.RecruitmentEmailTemplate;

public record RecruitmentEmailTemplateResponse(
		Long templateId,
		String content,
		boolean isSent,
		LocalDateTime sentAt,
		Long generationId,
		long subscriberCount) {

	public static RecruitmentEmailTemplateResponse of(
			RecruitmentEmailTemplate template, long subscriberCount) {
		return new RecruitmentEmailTemplateResponse(
				template.getId(),
				template.getContent(),
				template.isSent(),
				template.getSentAt(),
				template.getGeneration().getId(),
				subscriberCount);
	}

	/** 모집 중인 기수가 없을 때의 응답 */
	public static RecruitmentEmailTemplateResponse empty() {
		return new RecruitmentEmailTemplateResponse(null, "", false, null, null, 0L);
	}
}
