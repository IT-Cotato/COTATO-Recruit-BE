package org.cotato.backend.recruit.presentation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.common.util.DateFormatter;
import org.cotato.backend.recruit.domain.application.dto.ApplicationEtcData;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.entity.ApplicationEtcInfo;
import org.cotato.backend.recruit.domain.application.repository.ApplicationEtcInfoRepository;
import org.cotato.backend.recruit.presentation.dto.request.EtcAnswersRequest;
import org.cotato.backend.recruit.presentation.dto.response.EtcAnswerResponse;
import org.cotato.backend.recruit.presentation.dto.response.RecruitmentScheduleResponse;
import org.cotato.backend.recruit.presentation.error.PresentationErrorCode;
import org.cotato.backend.recruit.presentation.exception.PresentationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationEtcInfoService {

	private final ApplicationEtcInfoRepository applicationEtcInfoRepository;
	private final ApplicationService applicationService;
	private final RecruitmentService recruitmentService;
	private final ObjectMapper objectMapper;

	/**
	 * 기타 질문 조회 - 컨트롤러에서 호출
	 *
	 * @param userId        사용자 ID
	 * @param applicationId 지원서 ID
	 * @return 기타 질문 및 저장된 답변 응답
	 */
	public EtcAnswerResponse getEtcAnswers(Long userId, Long applicationId) {
		Application application = applicationService.getApplicationWithAuth(applicationId, userId);

		if (application.getIsSubmitted()) {
			throw new PresentationException(PresentationErrorCode.ALREADY_SUBMITTED);
		}

		// 모집 일정 조회
		RecruitmentScheduleResponse schedule = recruitmentService.getRecruitmentSchedule();

		// ApplicationEtcInfo에서 JSON 데이터 조회
		ApplicationEtcData etcData = getEtcData(application);

		// 날짜 포맷팅
		String interviewStartDate = DateFormatter.formatMonthDay(schedule.interviewStartDate());
		String interviewEndDate = DateFormatter.formatMonthDay(schedule.interviewEndDate());
		String otDate = DateFormatter.formatMonthDay(schedule.otDate());

		return EtcAnswerResponse.of(etcData, interviewStartDate, interviewEndDate, otDate);
	}

	/**
	 * 기타 질문 임시 저장 - 컨트롤러에서 호출
	 *
	 * @param userId        사용자 ID
	 * @param applicationId 지원서 ID
	 * @param request       기타 질문 응답 및 추가 정보 요청
	 */
	@Transactional
	public void saveEtcAnswers(Long userId, Long applicationId, EtcAnswersRequest request) {
		Application application = applicationService.getApplicationWithAuth(applicationId, userId);

		if (application.getIsSubmitted()) {
			throw new PresentationException(PresentationErrorCode.ALREADY_SUBMITTED);
		}

		// EtcAnswersRequest를 ApplicationEtcData로 변환
		ApplicationEtcData etcData = ApplicationEtcData.of(
				request.discoveryPath(),
				request.parallelActivities(),
				request.unavailableInterviewTimes(),
				request.sessionAttendanceAgreed(),
				request.mandatoryEventsAgreed(),
				request.privacyPolicyAgreed());

		// ApplicationEtcData를 JSON으로 변환
		String jsonData;
		try {
			jsonData = objectMapper.writeValueAsString(etcData);
		} catch (JsonProcessingException e) {
			throw new PresentationException(PresentationErrorCode.INVALID_JSON_FORMAT);
		}

		// ApplicationEtcInfo 조회 또는 생성
		ApplicationEtcInfo etcInfo = applicationEtcInfoRepository
				.findByApplication(application)
				.orElseGet(() -> ApplicationEtcInfo.createNew(application));

		// JSON 데이터 업데이트
		etcInfo.updateEtcData(jsonData);

		// 저장
		applicationEtcInfoRepository.save(etcInfo);
	}

	/**
	 * ApplicationEtcInfo에서 JSON 데이터를 ApplicationEtcData로 변환
	 *
	 * @param application 지원서
	 * @return ApplicationEtcData (없으면 모든 필드 null인 객체 반환)
	 */
	private ApplicationEtcData getEtcData(Application application) {
		Optional<ApplicationEtcInfo> etcInfoOpt = applicationEtcInfoRepository.findByApplication(application);

		if (etcInfoOpt.isEmpty() || etcInfoOpt.get().getEtcData() == null) {
			// 기타 정보가 없으면 모든 필드 null인 객체 반환
			return new ApplicationEtcData(null, null, null, null, null, null);
		}

		try {
			return objectMapper.readValue(etcInfoOpt.get().getEtcData(), ApplicationEtcData.class);
		} catch (JsonProcessingException e) {
			throw new PresentationException(PresentationErrorCode.INVALID_JSON_FORMAT);
		}
	}
}
