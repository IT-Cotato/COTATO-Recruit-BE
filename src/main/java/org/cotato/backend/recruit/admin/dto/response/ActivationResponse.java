package org.cotato.backend.recruit.admin.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivationResponse {
	private Long generation;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private boolean isActive;
}
