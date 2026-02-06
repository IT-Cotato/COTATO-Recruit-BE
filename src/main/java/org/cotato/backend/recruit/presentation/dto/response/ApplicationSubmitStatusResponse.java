package org.cotato.backend.recruit.presentation.dto.response;

import org.cotato.backend.recruit.domain.application.entity.Application;

public record ApplicationSubmitStatusResponse(
		Long applicationId, Boolean isSubmitted, Boolean isEnd) {

	public static ApplicationSubmitStatusResponse from(Application application, Boolean isEnd) {
		return new ApplicationSubmitStatusResponse(
				application.getId(), application.getIsSubmitted(), isEnd);
	}

	public static ApplicationSubmitStatusResponse noApplication(Boolean isEnd) {
		return new ApplicationSubmitStatusResponse(null, false, isEnd);
	}
}
