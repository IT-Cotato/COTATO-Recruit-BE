package org.cotato.backend.recruit.admin.dto.response.recruitmentActive;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ActivationResponse(
		Long generation, LocalDateTime startDate, LocalDateTime endDate, boolean isActive) {}
