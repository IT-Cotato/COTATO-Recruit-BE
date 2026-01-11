package org.cotato.backend.recruit.admin.dto.request.notice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecruitmentPartType {
	PM("Product Manager", "PM", "pm.jpg"),
	DE("Team Design", "DE", "design.jpg"),
	FE("Team Frontend", "FE", "frontend.jpg"),
	BE("Team Backend", "BE", "backend.jpg");

	private final String partName;
	private final String partShort;
	private final String imageFilename;
}
