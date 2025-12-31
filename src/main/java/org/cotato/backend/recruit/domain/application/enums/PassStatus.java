package org.cotato.backend.recruit.domain.application.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PassStatus {
	PASS("합격"),
	FAIL("불합격"),
	WAITLISTED("예비 합격"),
	PENDING("평가 전");

	private final String description;

	public static PassStatus fromString(String name) {
		isValidPassStatus(name);
		return valueOf(name);
	}

	public static void isValidPassStatus(String name) {
		if (name == null) {
			throw new IllegalArgumentException("PassStatus은 null일 수 없습니다.");
		}

		if (!name.equals(name.toUpperCase())) {
			throw new IllegalArgumentException("PassStatus은 대문자로 입력해야 합니다.");
		}

		if (!name.equals("PASS")
				&& !name.equals("FAIL")
				&& !name.equals("WAITLISTED")
				&& !name.equals("PENDING")) {
			throw new IllegalArgumentException("유효하지 않은 합격 여부입니다.");
		}
	}
}
