package org.cotato.backend.recruit.admin.dto.response.recruitmentInformation;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record RecruitmentPeriodResponse(
		LocalDateTime recruitmentStart, LocalDateTime recruitmentEnd) {}
