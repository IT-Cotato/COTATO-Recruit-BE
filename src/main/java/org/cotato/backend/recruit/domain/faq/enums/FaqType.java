package org.cotato.backend.recruit.domain.faq.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FaqType {
	COMMON("공통"),
	PM("기획"),
	DE("디자이너"),
	FE("프론트엔드"),
	BE("백엔드");

	private final String description;
}
