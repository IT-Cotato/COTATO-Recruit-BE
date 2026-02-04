package org.cotato.backend.recruit.admin.dto.response.email;

import java.time.LocalDateTime;
import org.cotato.backend.recruit.domain.email.enums.TemplateType;

public record EmailSendResponse(
		Long jobId, String templateType, long totalCount, LocalDateTime sentAt, Long generationId) {

	public static EmailSendResponse of(
			Long jobId,
			TemplateType templateType,
			int totalCount,
			LocalDateTime sentAt,
			Long generationId) {
		return new EmailSendResponse(jobId, templateType.name(), totalCount, sentAt, generationId);
	}
}
