package org.cotato.backend.recruit.admin.dto.request.notice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleType {
	APPLICATION("서류 접수"),
	DOCUMENT_RESULT("서류 합격 발표"),
	INTERVIEW("면접 평가"),
	FINAL_RESULT("최종 합격 발표"),
	OT("대면 OT");

	private final String scheduleTitle;

	/** 기수 정보를 포함한 전체 제목 반환 */
	public String getFullTitle(int generation) {
		return generation + "기 " + scheduleTitle;
	}
}
