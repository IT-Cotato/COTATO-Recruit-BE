package org.cotato.backend.recruit.admin.dto.response.applicationView;

import java.time.LocalDateTime;
import lombok.Builder;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.enums.ApplicationPartType;
import org.cotato.backend.recruit.domain.application.enums.PassStatus;

@Builder
public record ApplicationElementResponse(
		Long applicationId,
		String name,
		String gender,
		ApplicationPartType applicationPartType,
		String university,
		String phoneNumber,
		PassStatus passStatus,
		LocalDateTime submittedAt) {
	public static ApplicationElementResponse from(Application app) {
		return ApplicationElementResponse.builder()
				.applicationId(app.getId())
				.name(app.getName())
				.gender(app.getGender())
				.applicationPartType(app.getApplicationPartType())
				.university(app.getUniversity())
				.phoneNumber(app.getPhoneNumber())
				.passStatus(app.getPassStatus())
				.submittedAt(app.getSubmittedAt())
				.build();
	}
}
