package org.cotato.backend.recruit.presentation.dto.response;

import org.cotato.backend.recruit.domain.application.entity.Application;

public record ApplicationSubmitStatusResponse(
		Long applicationId, Boolean isSubmitted, Boolean isStart, Boolean isEnd) {

	public static ApplicationSubmitStatusResponse from(
			Application application, Boolean isStart, Boolean isEnd) {
		return new ApplicationSubmitStatusResponse(
				application.getId(), application.getIsSubmitted(), isStart, isEnd);
	}

	public static ApplicationSubmitStatusResponse noApplication(Boolean isStart, Boolean isEnd) {
		return new ApplicationSubmitStatusResponse(null, false, isStart, isEnd);
	}
}
