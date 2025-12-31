package org.cotato.backend.recruit.domain.recruitmentNotice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NoticeType {
	RECRUITMENT_SCHEDULE("모집일정"),
	RECRUITMENT_PART("모집파트"),
	ACTIVITY_SCHEDULE("주요활동일정");

	private final String description;
}
