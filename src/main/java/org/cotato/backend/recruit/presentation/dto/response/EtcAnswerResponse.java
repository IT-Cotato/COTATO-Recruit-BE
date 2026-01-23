package org.cotato.backend.recruit.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import java.util.List;
import org.cotato.backend.recruit.domain.application.dto.ApplicationEtcData;
import org.cotato.backend.recruit.domain.application.enums.DiscoveryPath;

@Schema(description = "기타 질문 답변 조회 응답 (임시저장된 데이터 포함)")
@JsonInclude(JsonInclude.Include.ALWAYS)
public record EtcAnswerResponse(
		@Schema(description = "동아리를 알게 된 경로") DiscoveryPathQuestion discoveryPath,
		@Schema(description = "병행 활동", nullable = true) String parallelActivities,
		@Schema(description = "면접 불가 시간", nullable = true) String unavailableInterviewTimes,
		@Schema(description = "세션 출석 동의", nullable = true) Boolean sessionAttendance,
		@Schema(description = "필수 행사 참여 동의", nullable = true) Boolean mandatoryEvents,
		@Schema(description = "개인정보 처리 동의", nullable = true) Boolean privacyPolicy,
		@Schema(description = "면접 시작 일자", example = "3월 3일") String interviewStartDate,
		@Schema(description = "면접 종료 일자", example = "3월 5일") String interviewEndDate,
		@Schema(description = "대면 OT 날짜", example = "3월 6일") String otDate) {

	public static EtcAnswerResponse of(
			ApplicationEtcData etcData,
			String interviewStartDate,
			String interviewEndDate,
			String otDate) {

		// 1. 동아리를 알게 된 경로 옵션 리스트 생성
		List<DiscoveryPathQuestion.DiscoveryPathOption> discoveryPathOptions =
				Arrays.stream(DiscoveryPath.values())
						.map(
								dp ->
										new DiscoveryPathQuestion.DiscoveryPathOption(
												dp.getDescription()))
						.toList();

		// 2. DiscoveryPathQuestion 객체 생성
		String selectedDiscoveryPath =
				etcData.discoveryPath() != null ? etcData.discoveryPath().getDescription() : null;
		DiscoveryPathQuestion discoveryPathQuestion =
				new DiscoveryPathQuestion(discoveryPathOptions, selectedDiscoveryPath);

		// 3. Record 생성자 순서에 맞춰 반환 (null 값 그대로 유지)
		return new EtcAnswerResponse(
				discoveryPathQuestion,
				etcData.parallelActivities(),
				etcData.unavailableInterviewTimes(),
				etcData.sessionAttendanceAgreed(),
				etcData.mandatoryEventsAgreed(),
				etcData.privacyPolicyAgreed(),
				interviewStartDate,
				interviewEndDate,
				otDate);
	}

	@Schema(description = "동아리를 알게 된 경로 질문 및 답변")
	public record DiscoveryPathQuestion(
			@Schema(description = "선택 가능한 경로 목록") List<DiscoveryPathOption> options,
			@Schema(description = "선택한 경로", example = "인스타그램", nullable = true)
					String selectedAnswer) {
		@Schema(description = "경로 옵션")
		public record DiscoveryPathOption(
				@Schema(description = "옵션 값", example = "인스타그램") String value) {}
	}
}
