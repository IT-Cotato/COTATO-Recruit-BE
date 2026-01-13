package org.cotato.backend.recruit.presentation.dto.response;

import java.time.LocalDate;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.enums.ApplicationPartType;

public record BasicInfoResponse(
		Long applicationId,
		String name,
		String gender,
		LocalDate birthDate,
		String phoneNumber,
		String university,
		String major,
		Integer completedSemesters,
		Boolean isPrevActivity,
		boolean isEnrolled,
		ApplicationPartType applicationPartType) {

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
				application.getIsPrevActivity(),
				application.getIsEnrolled(),
				application.getApplicationPartType());
	}
}
