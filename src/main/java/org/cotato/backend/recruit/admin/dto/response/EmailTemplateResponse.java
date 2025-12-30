package org.cotato.backend.recruit.admin.dto.response;

import java.time.LocalDateTime;
import org.cotato.backend.recruit.domain.email.entity.EmailTemplate;

public record EmailTemplateResponse(
		Long templateId,
		String templateType,
		String templateTypeDescription,
		String content,
		boolean isSent,
		LocalDateTime sentAt,
		Long generationId,
		long recipientCount) {

	public static EmailTemplateResponse from(EmailTemplate emailTemplate) {
		return new EmailTemplateResponse(
				emailTemplate.getId(),
				emailTemplate.getTemplateType().name(),
				emailTemplate.getTemplateType().getDescription(),
				emailTemplate.getContent(),
				emailTemplate.isSent(),
				emailTemplate.getSentAt(),
				emailTemplate.getGeneration().getId(),
				0L);
	}

	public static EmailTemplateResponse of(EmailTemplate emailTemplate, long recipientCount) {
		return new EmailTemplateResponse(
				emailTemplate.getId(),
				emailTemplate.getTemplateType().name(),
				emailTemplate.getTemplateType().getDescription(),
				emailTemplate.getContent(),
				emailTemplate.isSent(),
				emailTemplate.getSentAt(),
				emailTemplate.getGeneration().getId(),
				recipientCount);
	}

	/** 모집 중인 기수가 없을 때의 응답 */
	public static EmailTemplateResponse empty(String templateType, String description) {
		return new EmailTemplateResponse(
				null, templateType, description, "", false, null, null, 0L);
	}
}
