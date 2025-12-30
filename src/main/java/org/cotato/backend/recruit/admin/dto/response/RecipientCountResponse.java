package org.cotato.backend.recruit.admin.dto.response;

import org.cotato.backend.recruit.domain.application.enums.PassStatus;
import org.cotato.backend.recruit.domain.email.enums.TemplateType;

public record RecipientCountResponse(
		String templateType, String passStatus, long count, Long generationId) {

	public static RecipientCountResponse of(
			TemplateType templateType, PassStatus passStatus, long count, Long generationId) {
		return new RecipientCountResponse(
				templateType.name(), passStatus.name(), count, generationId);
	}
}
