package org.cotato.backend.recruit.domain.application.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AnswerType {
	CHECKBOX("체크박스"),
	TEXT("텍스트"),
	FILE("파일");

	private final String description;
}
