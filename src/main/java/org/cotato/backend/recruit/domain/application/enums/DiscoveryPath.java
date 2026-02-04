package org.cotato.backend.recruit.domain.application.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DiscoveryPath {
	INSTAGRAM("인스타그램"),
	NAVER_CAFE("네이버 카페"),
	OTHER_SNS("그 외 SNS"),
	EVERYTIME("에브리타임"),
	CAMPUSPICK("캠퍼스픽"),
	FRIEND_REFERRAL("지인 소개"),
	JIKHAENG("직행"),
	NONE("해당 없음");

	private final String description;
}
