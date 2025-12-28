package org.cotato.backend.recruit.domain.recruitment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InformationType {
	RECRUITMENT_START("지원시작"),
	RECRUITMENT_END("지원종료"),
	DOCUMENT_ANNOUNCEMENT("서류발표"),
	INTERVIEW_START("면접평가시작"),
	INTERVIEW_END("면접평가종료"),
	FINAL_ANNOUNCEMENT("최종발표"),
	OT("OT");

	private final String description;
}
