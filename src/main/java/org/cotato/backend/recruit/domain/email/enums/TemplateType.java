package org.cotato.backend.recruit.domain.email.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TemplateType {
	PASS("합격"),
	FAIL("불합격"),
	PRELIMINARY("예비");

	private final String description;
}
