package org.cotato.backend.recruit.presentation.dto.response;

import java.time.LocalDate;
import lombok.Builder;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.enums.ApplicationPartType;

@Builder
public record SubmittedApplicationBasicInfoResponse(
		Long applicationId,
		String name,
		String gender,
		LocalDate birthDate,
		String phoneNumber,
		String school,
		String major,
		Boolean isEnrolled,
		Integer completedSemesters,
		Boolean isPrevActivity,
		ApplicationPartType applicationPartType) {
	public static SubmittedApplicationBasicInfoResponse from(Application application) {
		return SubmittedApplicationBasicInfoResponse.builder()
				.applicationId(application.getId())
				.name(application.getName())
				.gender(application.getGender())
				.birthDate(application.getBirthDate())
				.phoneNumber(application.getPhoneNumber())
				.school(application.getUniversity())
				.major(application.getMajor())
				.isEnrolled(application.getIsEnrolled())
				.completedSemesters(application.getCompletedSemesters())
				.isPrevActivity(application.getIsPrevActivity())
				.applicationPartType(application.getApplicationPartType())
				.build();
	}
}
