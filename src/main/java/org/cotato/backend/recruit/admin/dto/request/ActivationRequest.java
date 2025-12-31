package org.cotato.backend.recruit.admin.dto.request;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ActivationRequest {
	private Long generation;
	private LocalDate startDate;
	private LocalDate endDate;
}
