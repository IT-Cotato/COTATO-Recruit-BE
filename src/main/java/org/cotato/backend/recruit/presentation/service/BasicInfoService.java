package org.cotato.backend.recruit.presentation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.repository.ApplicationRepository;
import org.cotato.backend.recruit.presentation.dto.request.BasicInfoRequest;
import org.cotato.backend.recruit.presentation.dto.response.BasicInfoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicInfoService {

	private final ApplicationRepository applicationRepository;
	private final ApplicationService applicationService;

	/**
	 * 기본 인적사항 작성(임시저장)
	 *
	 * @param userId 사용자 ID
	 * @param applicationId 지원서 ID
	 * @param request 기본 인적사항 요청
	 */
	@Transactional
	public void saveBasicInfo(Long userId, Long applicationId, BasicInfoRequest request) {
		Application application = applicationService.getApplicationWithAuth(applicationId, userId);

		application.updateBasicInfo(
				request.name(),
				request.gender(),
				request.birthDate(),
				request.phoneNumber(),
				request.university(),
				request.major(),
				request.completedSemesters(),
				request.isPrevActivity(),
				request.applicationPartType());

		applicationRepository.save(application);
	}

	/**
	 * 기본 인적사항 조회
	 *
	 * @param userId 사용자 ID
	 * @param applicationId 지원서 ID
	 * @return 기본 인적사항 응답
	 */
	public BasicInfoResponse getBasicInfo(Long userId, Long applicationId) {
		Application application = applicationService.getApplicationWithAuth(applicationId, userId);

		return BasicInfoResponse.from(application);
	}
}
