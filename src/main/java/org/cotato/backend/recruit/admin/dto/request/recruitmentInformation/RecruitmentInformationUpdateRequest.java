package org.cotato.backend.recruit.admin.dto.request.recruitmentInformation;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RecruitmentInformationUpdateRequest(
		@NotNull Long generationId,
		@NotNull LocalDateTime recruitmentStart,
		@NotNull LocalDateTime recruitmentEnd,
		@NotNull LocalDate documentAnnouncement,
		@NotNull LocalDate interviewStart,
		@NotNull LocalDate interviewEnd,
		@NotNull LocalDate finalAnnouncement,
		@NotNull LocalDate ot) {}
