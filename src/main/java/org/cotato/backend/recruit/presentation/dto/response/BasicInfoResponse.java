package org.cotato.backend.recruit.presentation.dto.response;

import java.time.LocalDate;
import org.cotato.backend.recruit.domain.application.entity.Application;

public record BasicInfoResponse(
		Long applicationId,
		String name,
		String gender,
		LocalDate birthDate,
		String phoneNumber,
		String university,
		String major,
		Integer completedSemesters,
		Boolean isPrevActivity) {

	public static BasicInfoResponse from(Application application) {
		return new BasicInfoResponse(
				application.getId(),
				application.getName(),
				application.getGender(),
				application.getBirthDate(),
				application.getPhoneNumber(),
				application.getUniversity(),
				application.getMajor(),
				application.getCompletedSemesters(),
				application.getIsPrevActivity());
	}
}
