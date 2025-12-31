package org.cotato.backend.recruit.admin.dto.response.email;

import java.time.LocalDateTime;
import org.cotato.backend.recruit.domain.email.enums.TemplateType;

public record EmailSendResponse(
		String templateType,
		long successCount,
		long failCount,
		LocalDateTime sentAt,
		Long generationId) {

	public static EmailSendResponse of(
			TemplateType templateType,
			long successCount,
			long failCount,
			LocalDateTime sentAt,
			Long generationId) {
		return new EmailSendResponse(
				templateType.name(), successCount, failCount, sentAt, generationId);
	}
}
