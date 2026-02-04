package org.cotato.backend.recruit.presentation.dto.response;

import org.cotato.backend.recruit.domain.application.entity.Application;

public record ApplicationStartResponse(Long applicationId, boolean isSubmitted) {

	public static ApplicationStartResponse from(Application application) {
		return new ApplicationStartResponse(application.getId(), application.getIsSubmitted());
	}
}
