package org.cotato.backend.recruit.domain.application.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DiscoveryPath {
	SNS("SNS"),
	FRIEND_REFERRAL("지인 추천"),
	SCHOOL_PROMOTION("학교 홍보"),
	OTHER("기타");

	private final String description;
}
