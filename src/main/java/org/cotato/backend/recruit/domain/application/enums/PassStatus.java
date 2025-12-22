package org.cotato.backend.recruit.domain.application.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PassStatus {
	PASS("합격"),
	FAIL("불합격"),
	WAITLISTED("예비 합격"),
	PENDING("평가 전");

	private final String description;
}
