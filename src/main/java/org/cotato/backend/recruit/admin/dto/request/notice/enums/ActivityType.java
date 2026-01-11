package org.cotato.backend.recruit.admin.dto.request.notice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityType {
	OT("OT", "ot.jpg"),
	REGULAR_SESSION("정기세션", "정기-세션.jpg"),
	MT("MT", "mt.jpg"),
	DEV_TALK("Dev Talk", "devtalk.jpg"),
	COKATHON("코카톤", "코커톤.jpg"),
	DEMO_DAY("데모데이", "데모데이.jpg");

	private final String activityName;
	private final String imageFilename;
}
