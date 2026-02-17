package org.cotato.backend.recruit.presentation.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.common.error.ErrorCode;
import org.cotato.backend.recruit.common.exception.GlobalException;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.repository.ApplicationRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.question.entity.Question;
import org.cotato.backend.recruit.domain.question.enums.QuestionType;
import org.cotato.backend.recruit.domain.user.entity.User;
import org.cotato.backend.recruit.domain.user.repository.UserRepository;
import org.cotato.backend.recruit.presentation.dto.response.ApplicationSubmitStatusResponse;
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
	private final RecruitmentService recruitmentService;
	private final QuestionService questionService;

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
	 */
	@Transactional
	public void startApplication(Long userId) {
		User user =
				userRepository
						.findById(userId)
						.orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

		// 현재 모집 중인 기수 조회
		Generation activeGeneration = generationService.getActiveGeneration();

		// 지원 기간 검증 (시작 + 종료)
		recruitmentService.validateRecruitmentPeriod(activeGeneration);

		// 이미 해당 기수에 지원서가 있는지 확인
		Optional<Application> existingApplication =
				applicationRepository.findByUserAndGeneration(user, activeGeneration);

		if (existingApplication.isPresent()) {
			if (existingApplication.get().getIsSubmitted()) {
				throw new PresentationException(PresentationErrorCode.ALREADY_SUBMITTED);
			}
			return;
		}

		// 지원서가 없으면 새로 생성
		Application newApplication = Application.createNew(user, activeGeneration);
		applicationRepository.save(newApplication);
	}

	/**
	 * 지원서 상태 조회
	 *
	 * @param userId 사용자 ID
	 * @return 지원서 상태 응답 (지원서 ID, 제출 여부, 모집 시작/종료 여부)
	 */
	public ApplicationSubmitStatusResponse getApplicationStatus(Long userId) {
		User user =
				userRepository
						.findById(userId)
						.orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

		// 현재 모집 중인 기수 조회
		Generation activeGeneration = generationService.getActiveGeneration();

		// 모집 시작 여부 확인
		Boolean isStart = recruitmentService.isRecruitmentStarted(activeGeneration);

		// 모집 종료 여부 확인
		Boolean isEnd = recruitmentService.isRecruitmentEnded(activeGeneration);

		// 이미 해당 기수에 지원서가 있는지 확인
		Optional<Application> existingApplication =
				applicationRepository.findByUserAndGeneration(user, activeGeneration);

		if (existingApplication.isEmpty()) {
			// 지원서가 없으면 null 반환
			return ApplicationSubmitStatusResponse.noApplication(isStart, isEnd);
		}

		// 지원서가 있으면 ID와 제출 여부 반환
		return ApplicationSubmitStatusResponse.from(existingApplication.get(), isStart, isEnd);
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

		// 지원 기간 검증 (시작 + 종료)
		recruitmentService.validateRecruitmentPeriod(application.getGeneration());

		// 지원한 파트의 질문 조회
		List<Question> partQuestions = getRequiredQuestions(application);

		application.submit(partQuestions);
		applicationRepository.save(application);
	}

	private List<Question> getRequiredQuestions(Application application) {

		if (application.getApplicationPartType() == null) {
			throw new PresentationException(PresentationErrorCode.PART_TYPE_NOT_SELECTED);
		}

		QuestionType questionType = application.getApplicationPartType().toQuestionType();

		return questionService.getQuestionsByGenerationAndQuestionType(
				application.getGeneration(), questionType);
	}
}
