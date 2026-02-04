package org.cotato.backend.recruit.admin.dto.response.recruitmentInformation;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record RecruitmentPeriodResponse(
		LocalDateTime recruitmentStart, LocalDateTime recruitmentEnd) {

	public static RecruitmentPeriodResponse of(
			LocalDateTime recruitmentStart, LocalDateTime recruitmentEnd) {
		return RecruitmentPeriodResponse.builder()
				.recruitmentStart(recruitmentStart)
				.recruitmentEnd(recruitmentEnd)
				.build();
	}
}
