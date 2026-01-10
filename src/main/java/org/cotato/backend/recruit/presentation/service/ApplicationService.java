package org.cotato.backend.recruit.presentation.service;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.common.error.ErrorCode;
import org.cotato.backend.recruit.common.exception.GlobalException;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.repository.ApplicationRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.cotato.backend.recruit.domain.recruitmentInformation.repository.RecruitmentInformationRepository;
import org.cotato.backend.recruit.domain.user.entity.User;
import org.cotato.backend.recruit.domain.user.repository.UserRepository;
import org.cotato.backend.recruit.presentation.dto.response.ApplicationStartResponse;
import org.cotato.backend.recruit.presentation.error.PresentationErrorCode;
import org.cotato.backend.recruit.presentation.exception.PresentationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

	private final ApplicationRepository applicationRepository;
	private final UserRepository userRepository;
	private final GenerationService generationService;
	private final RecruitmentInformationRepository recruitmentInformationRepository;

	/**
	 * 지원서 권한 확인 및 조회
	 *
	 * @param applicationId 지원서 ID
	 * @param userId 사용자 ID
	 * @return 지원서
	 */
	public Application getApplicationWithAuth(Long applicationId, Long userId) {
		Application application =
				applicationRepository
						.findById(applicationId)
						.orElseThrow(
								() ->
										new PresentationException(
												PresentationErrorCode.APPLICATION_NOT_FOUND));

		application.validateUser(userId);

		return application;
	}

	/**
	 * 지원서 시작 (지원하기 버튼 클릭) 이미 해당 기수에 지원서가 있으면 기존 지원서 반환, 없으면 새로 생성
	 *
	 * @param userId 사용자 ID
	 * @return 지원서 시작 응답 (지원서 ID, 신규 생성 여부)
	 */
	@Transactional
	public ApplicationStartResponse startApplication(Long userId) {
		User user =
				userRepository
						.findById(userId)
						.orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

		// 현재 모집 중인 기수 조회
		Generation activeGeneration = generationService.getActiveGeneration();

		// 이미 해당 기수에 지원서가 있는지 확인
		Optional<Application> existingApplication =
				applicationRepository.findByUserAndGeneration(user, activeGeneration);

		if (existingApplication.isPresent()) {
			// 기존 지원서가 있으면 해당 ID와 제출 여부 반환
			Application application = existingApplication.get();

			// 이미 제출한 경우 예외 발생
			if (application.isSubmitted()) {
				throw new PresentationException(
						PresentationErrorCode.APPLICATION_ALREADY_SUBMITTED);
			}

			return ApplicationStartResponse.from(application);
		}

		// 지원서가 없으면 새로 생성
		Application newApplication = Application.createNew(user, activeGeneration);
		Application savedApplication = applicationRepository.save(newApplication);

		return ApplicationStartResponse.from(savedApplication);
	}

	/**
	 * 지원서 최종 제출
	 *
	 * @param userId 사용자 ID
	 * @param applicationId 지원서 ID
	 */
	@Transactional
	public void submitApplication(Long userId, Long applicationId) {
		Application application = getApplicationWithAuth(applicationId, userId);

		// 지원 기간 검증
		validateRecruitmentPeriod(application.getGeneration());

		application.submit();
		applicationRepository.save(application);
	}

	/**
	 * 지원 기간 검증
	 *
	 * @param generation 기수
	 */
	private void validateRecruitmentPeriod(Generation generation) {
		RecruitmentInformation recruitmentEnd =
				recruitmentInformationRepository
						.findByGenerationAndInformationType(
								generation, InformationType.RECRUITMENT_END)
						.orElseThrow(
								() ->
										new PresentationException(
												PresentationErrorCode.GENERATION_NOT_FOUND));

		LocalDateTime now = LocalDateTime.now();
		if (now.isAfter(recruitmentEnd.getEventDatetime())) {
			throw new PresentationException(PresentationErrorCode.RECRUITMENT_PERIOD_ENDED);
		}
	}
}
