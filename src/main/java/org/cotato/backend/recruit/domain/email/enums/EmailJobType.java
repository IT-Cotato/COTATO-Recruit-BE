package org.cotato.backend.recruit.domain.email.enums;

public enum EmailJobType {
	PASS,
	FAIL,
	PRELIMINARY,
	RECRUITMENT_NOTIFICATION;

	public static EmailJobType from(TemplateType templateType) {
		return switch (templateType) {
			case PASS -> PASS;
			case FAIL -> FAIL;
			case PRELIMINARY -> PRELIMINARY;
		};
	}
}
