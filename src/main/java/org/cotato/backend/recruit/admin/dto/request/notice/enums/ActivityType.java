package org.cotato.backend.recruit.admin.dto.request.notice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityType {
	OT("OT", "OT"),
	REGULAR_SESSION("정기세션", "SESSION"),
	MT("MT", "MT"),
	DEV_TALK("Dev Talk", "DEVTALK"),
	COKATHON("코카톤", "COKERTHON"),
	DEMO_DAY("데모데이", "DEMODAY");

	private final String activityName;
	private final String activityShort;
}
