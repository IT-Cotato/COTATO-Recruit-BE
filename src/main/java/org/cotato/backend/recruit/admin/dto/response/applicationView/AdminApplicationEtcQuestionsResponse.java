package org.cotato.backend.recruit.admin.dto.response.applicationView;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.enums.DiscoveryPath;
import org.cotato.backend.recruit.presentation.dto.response.EtcQuestionsResponse;

@Schema(description = "관리자용 기타 질문 및 답변 응답")
public record AdminApplicationEtcQuestionsResponse(
		@Schema(description = "동아리를 알게 된 경로")
				EtcQuestionsResponse.DiscoveryPathQuestion discoveryPath,
		@Schema(description = "병행 활동") String parallelActivities,
		@Schema(description = "면접 불가 시간") String unavailableInterviewTimes,
		@Schema(description = "세션 출석 동의") Boolean sessionAttendance,
		@Schema(description = "필수 행사 참여 동의") Boolean mandatoryEvents,
		@Schema(description = "개인정보 처리 동의") Boolean privacyPolicy,
		@Schema(description = "면접 시작 일자", example = "3월 3일") String interviewStartDate,
		@Schema(description = "면접 종료 일자", example = "3월 5일") String interviewEndDate,
		@Schema(description = "대면 OT 날짜", example = "3월 6일") String otDate) {

	public static AdminApplicationEtcQuestionsResponse of(
			Application application,
			LocalDateTime interviewStart,
			LocalDateTime interviewEnd,
			LocalDateTime ot) {

		// 1. 날짜 포맷팅
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일");
		String startDate = interviewStart.format(formatter);
		String endDate = interviewEnd.format(formatter);
		String formattedOtDate = ot.format(formatter);

		// 2. 동아리를 알게 된 경로 옵션 리스트 생성
		List<EtcQuestionsResponse.DiscoveryPathQuestion.DiscoveryPathOption> discoveryPathOptions =
				Arrays.stream(DiscoveryPath.values())
						.map(
								dp ->
										new EtcQuestionsResponse.DiscoveryPathQuestion
												.DiscoveryPathOption(dp.getDescription()))
						.toList();

		// 3. DiscoveryPathQuestion 객체 생성
		String selectedDiscoveryPath =
				application.getDiscoveryPath() != null
						? application.getDiscoveryPath().name()
						: null;
		EtcQuestionsResponse.DiscoveryPathQuestion discoveryPathQuestion =
				new EtcQuestionsResponse.DiscoveryPathQuestion(
						discoveryPathOptions, selectedDiscoveryPath);

		return new AdminApplicationEtcQuestionsResponse(
				discoveryPathQuestion,
				application.getParallelActivities(),
				application.getUnavailableInterviewTimes(),
				application.getSessionAttendanceAgreed(),
				application.getMandatoryEventsAgreed(),
				application.getPrivacyPolicyAgreed(),
				startDate,
				endDate,
				formattedOtDate);
	}
}
