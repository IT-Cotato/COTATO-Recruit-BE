package org.cotato.backend.recruit.admin.dto.request.recruitmentInformation;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RecruitmentInformationUpdateRequest(
		Long generation,
		LocalDateTime recruitmentStart,
		LocalDateTime recruitmentEnd,
		LocalDate documentAnnouncement,
		LocalDate interviewStart,
		LocalDate interviewEnd,
		LocalDate finalAnnouncement,
		LocalDate ot) {}
