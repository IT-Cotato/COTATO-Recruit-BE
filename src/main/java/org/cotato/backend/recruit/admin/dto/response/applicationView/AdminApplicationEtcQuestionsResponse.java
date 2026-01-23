package org.cotato.backend.recruit.admin.dto.response.applicationView;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.cotato.backend.recruit.common.util.DateFormatter;
import org.cotato.backend.recruit.domain.application.dto.ApplicationEtcData;
import org.cotato.backend.recruit.domain.application.enums.DiscoveryPath;

@Schema(description = "관리자용 기타 질문 및 답변 응답")
public record AdminApplicationEtcQuestionsResponse(
		@Schema(description = "동아리를 알게 된 경로") DiscoveryPathQuestion discoveryPath,
		@Schema(description = "병행 활동") String parallelActivities,
		@Schema(description = "면접 불가 시간") String unavailableInterviewTimes,
		@Schema(description = "세션 출석 동의") Boolean sessionAttendance,
		@Schema(description = "필수 행사 참여 동의") Boolean mandatoryEvents,
		@Schema(description = "개인정보 처리 동의") Boolean privacyPolicy,
		@Schema(description = "면접 시작 일자", example = "3월 3일") String interviewStartDate,
		@Schema(description = "면접 종료 일자", example = "3월 5일") String interviewEndDate,
		@Schema(description = "대면 OT 날짜", example = "3월 6일") String otDate) {

	public static AdminApplicationEtcQuestionsResponse of(
			ApplicationEtcData etcData,
			LocalDateTime interviewStart,
			LocalDateTime interviewEnd,
			LocalDateTime ot) {

		// 1. 날짜 포맷팅
		String startDate = DateFormatter.formatMonthDay(interviewStart);
		String endDate = DateFormatter.formatMonthDay(interviewEnd);
		String formattedOtDate = DateFormatter.formatMonthDay(ot);

		// 2. 동아리를 알게 된 경로 옵션 리스트 생성
		List<DiscoveryPathQuestion.DiscoveryPathOption> discoveryPathOptions =
				Arrays.stream(DiscoveryPath.values())
						.map(
								dp ->
										new DiscoveryPathQuestion.DiscoveryPathOption(
												dp.getDescription()))
						.toList();

		// 3. DiscoveryPathQuestion 객체 생성
		String selectedDiscoveryPath =
				etcData.discoveryPath() != null ? etcData.discoveryPath().getDescription() : null;
		DiscoveryPathQuestion discoveryPathQuestion =
				new DiscoveryPathQuestion(discoveryPathOptions, selectedDiscoveryPath);

		return new AdminApplicationEtcQuestionsResponse(
				discoveryPathQuestion,
				etcData.parallelActivities(),
				etcData.unavailableInterviewTimes(),
				etcData.sessionAttendanceAgreed(),
				etcData.mandatoryEventsAgreed(),
				etcData.privacyPolicyAgreed(),
				startDate,
				endDate,
				formattedOtDate);
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
