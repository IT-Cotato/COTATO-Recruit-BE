package org.cotato.backend.recruit.admin.dto.response.applicationView;

import java.time.LocalDate;
import lombok.Builder;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.enums.ApplicationPartType;

@Builder
public record AdminApplicationBasicInfoResponse(
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
	public static AdminApplicationBasicInfoResponse from(Application application) {
		return AdminApplicationBasicInfoResponse.builder()
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
