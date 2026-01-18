package org.cotato.backend.recruit.common.email.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailHtmlTemplateType {
	RECRUITMENT_NOTIFICATION("templates/email/recruitment-notification.html"),
	PASS_RESULT("templates/email/pass-result.html");

	private final String templatePath;
}
