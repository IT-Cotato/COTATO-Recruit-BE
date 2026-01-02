package org.cotato.backend.recruit.admin.dto.response.recruitmentInformation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record RecruitmentInformationResponse(
		LocalDateTime recruitmentStart,
		LocalDateTime recruitmentEnd,
		LocalDate documentAnnouncement,
		LocalDate interviewStart,
		LocalDate interviewEnd,
		LocalDate finalAnnouncement,
		LocalDate ot) {}
