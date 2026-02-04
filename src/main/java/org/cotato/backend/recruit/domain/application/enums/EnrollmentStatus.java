package org.cotato.backend.recruit.domain.application.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnrollmentStatus {
	ENROLLED("재학"),
	NOT_ENROLLED("휴학/졸업/수료"); // 필요에 따라 구분 가능

	private final String description;

	// boolean 값을 받아 알맞은 Enum을 반환하는 메서드 (편의성)
	public static EnrollmentStatus from(boolean isEnrolled) {
		return isEnrolled ? ENROLLED : NOT_ENROLLED;
	}
}
