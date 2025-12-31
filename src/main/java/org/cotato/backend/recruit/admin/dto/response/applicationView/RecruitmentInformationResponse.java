package org.cotato.backend.recruit.admin.dto.response.applicationView;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record RecruitmentInformationResponse(
		LocalDateTime recruitmentStart, LocalDateTime recruitmentEnd) {}
