package org.cotato.backend.recruit.admin.dto.request.notice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecruitmentPartType {
	PM("Product Manager", "PM"),
	DE("Team Design", "DE"),
	FE("Team Frontend", "FE"),
	BE("Team Backend", "BE");

	private final String partName;
	private final String partShort;
}
