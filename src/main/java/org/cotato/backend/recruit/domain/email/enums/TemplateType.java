package org.cotato.backend.recruit.domain.email.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.cotato.backend.recruit.domain.application.enums.PassStatus;

@Getter
@AllArgsConstructor
public enum TemplateType {
	PASS("합격"),
	FAIL("불합격"),
	PRELIMINARY("예비");

	private final String description;

	public PassStatus toPassStatus() {
		return switch (this) {
			case PASS -> PassStatus.PASS;
			case FAIL -> PassStatus.FAIL;
			case PRELIMINARY -> PassStatus.WAITLISTED;
		};
	}
}
