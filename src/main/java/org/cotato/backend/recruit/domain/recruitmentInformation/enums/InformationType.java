package org.cotato.backend.recruit.domain.recruitmentInformation.enums;

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
	OT("OT"),
	COKERTHON("코커톤"),
	DEMO_DAY("데모데이");

	private final String description;
}
