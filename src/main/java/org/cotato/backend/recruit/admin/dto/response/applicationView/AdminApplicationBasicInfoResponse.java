package org.cotato.backend.recruit.admin.dto.response.applicationView;

import java.time.LocalDate;
import lombok.Builder;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.enums.EnrollmentStatus;

@Builder
public record AdminApplicationBasicInfoResponse(
		Long applicationId,
		String name,
		String gender,
		LocalDate birthDate,
		String phoneNumber,
		String school,
		String major,
		EnrollmentStatus enrollmentStatus,
		Integer completedSemesters,
		Boolean isPrevActivity) {
	public static AdminApplicationBasicInfoResponse from(Application application) {
		return AdminApplicationBasicInfoResponse.builder()
				.applicationId(application.getId())
				.name(application.getName())
				.gender(application.getGender())
				.birthDate(application.getBirthDate())
				.phoneNumber(application.getPhoneNumber())
				.school(application.getUniversity())
				.major(application.getMajor())
				.enrollmentStatus(
						application.getIsEnrolled()
								? EnrollmentStatus.ENROLLED
								: EnrollmentStatus.NOT_ENROLLED)
				.completedSemesters(application.getCompletedSemesters())
				.isPrevActivity(application.getIsPrevActivity())
				.build();
	}
}
