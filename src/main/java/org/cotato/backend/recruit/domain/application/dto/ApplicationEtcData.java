package org.cotato.backend.recruit.domain.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.cotato.backend.recruit.domain.application.enums.DiscoveryPath;

/**
 * 기타 질문 응답 데이터 (JSON으로 저장) - 임시저장: null 필드는 JSON에서 제외됨 - 최종 제출: validateRequiredFields()로 필수 항목 검증
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApplicationEtcData(
		DiscoveryPath discoveryPath,
		String parallelActivities,
		String unavailableInterviewTimes,
		Boolean sessionAttendanceAgreed,
		Boolean mandatoryEventsAgreed,
		Boolean privacyPolicyAgreed) {

	// 정적 팩토리 메서드
	public static ApplicationEtcData of(
			DiscoveryPath discoveryPath,
			String parallelActivities,
			String unavailableInterviewTimes,
			Boolean sessionAttendanceAgreed,
			Boolean mandatoryEventsAgreed,
			Boolean privacyPolicyAgreed) {
		return new ApplicationEtcData(
				discoveryPath,
				parallelActivities,
				unavailableInterviewTimes,
				sessionAttendanceAgreed,
				mandatoryEventsAgreed,
				privacyPolicyAgreed);
	}
}
