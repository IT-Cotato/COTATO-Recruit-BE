package org.cotato.backend.recruit.domain.question.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuestionType {
	PM("기획"),
	DE("디자이너"),
	FE("프론트엔드"),
	BE("백엔드");

	private final String description;

	// from String
	public static QuestionType fromString(String name) {
		isValidQuestionType(name);
		return valueOf(name);
	}

	// validate
	public static void isValidQuestionType(String name) {
		if (name == null) {
			throw new IllegalArgumentException("QuestionType은 null일 수 없습니다.");
		}

		if (!name.equals(name.toUpperCase())) {
			throw new IllegalArgumentException("QuestionType은 대문자로 입력해야 합니다.");
		}

		// BE,FE,DE,PM
		if (!name.equals("BE") && !name.equals("FE") && !name.equals("DE") && !name.equals("PM")) {
			throw new IllegalArgumentException("유효하지 않은 파트 타입입니다.");
		}
	}
}
