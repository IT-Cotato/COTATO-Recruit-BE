package org.cotato.backend.recruit.presentation.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.entity.ApplicationAnswer;
import org.cotato.backend.recruit.domain.application.enums.AnswerType;
import org.cotato.backend.recruit.domain.application.repository.ApplicationAnswerRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.question.entity.Question;
import org.cotato.backend.recruit.domain.question.enums.PartType;
import org.cotato.backend.recruit.presentation.dto.request.AnswerRequest;
import org.cotato.backend.recruit.presentation.dto.response.AnswerResponse;
import org.cotato.backend.recruit.presentation.dto.response.QuestionWithAnswerResponse;
import org.cotato.backend.recruit.presentation.error.ApplicationErrorCode;
import org.cotato.backend.recruit.presentation.exception.ApplicationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationAnswerService {

	private final ApplicationAnswerRepository applicationAnswerRepository;
	private final QuestionService questionService;
	private final GenerationService generationService;
	private final ApplicationService applicationService;

	/**
	 * 질문 목록을 저장된 답변과 함께 매핑
	 *
	 * @param questions 질문 목록
	 * @param savedAnswers 저장된 답변 목록
	 * @return 질문과 답변이 매핑된 응답 목록
	 */
	private List<QuestionWithAnswerResponse> mapQuestionsWithAnswers(
			List<Question> questions, List<ApplicationAnswer> savedAnswers) {
		// 질문 ID를 키로 하는 답변 맵 생성
		Map<Long, ApplicationAnswer> answerMap =
				savedAnswers.stream()
						.collect(
								Collectors.toMap(
										answer -> answer.getQuestion().getId(), answer -> answer));

		// 질문과 저장된 답변을 함께 반환
		return questions.stream()
				.map(
						q -> {
							ApplicationAnswer savedAnswer = answerMap.get(q.getId());
							AnswerResponse answerResponse =
									savedAnswer != null
											? new AnswerResponse(
													savedAnswer.getId(),
													savedAnswer.getQuestion().getId(),
													savedAnswer.getAnswerType().name(),
													savedAnswer.getIsChecked(),
													savedAnswer.getContent(),
													savedAnswer.getFileKey(),
													savedAnswer.getFileUrl())
											: null;

							return new QuestionWithAnswerResponse(
									q.getId(),
									q.getSequence(),
									q.getContent(),
									q.getPartType().name(),
									q.getAnswerType().name(),
									q.getMaxLength(),
									answerResponse);
						})
				.collect(Collectors.toList());
	}

	/**
	 * 파트별 질문 조회 (공통 + 선택한 파트만) + 저장된 답변 포함 - 페이지 2용
	 *
	 * @param userId 사용자 ID
	 * @param applicationId 지원서 ID
	 * @param partType 파트 타입 (PM, DE, FE, BE)
	 * @return 질문 및 저장된 답변 목록
	 */
	public List<QuestionWithAnswerResponse> getQuestionsWithAnswers(
			Long userId, Long applicationId, String partType) {
		Application application = applicationService.getApplicationWithAuth(applicationId, userId);
		Generation activeGeneration = generationService.getActiveGeneration();
		PartType selectedPart = PartType.valueOf(partType.toUpperCase());

		// 공통 질문 조회
		List<Question> commonQuestions =
				questionService.getQuestionsByGenerationAndPartType(
						activeGeneration, PartType.COMMON);

		// 선택한 파트 질문 조회
		List<Question> partQuestions =
				questionService.getQuestionsByGenerationAndPartType(activeGeneration, selectedPart);

		// 공통 + 파트별 질문 합치기
		List<Question> allQuestions = new ArrayList<>();
		allQuestions.addAll(commonQuestions);
		allQuestions.addAll(partQuestions);

		// 저장된 답변 조회 및 매핑
		List<ApplicationAnswer> savedAnswers =
				applicationAnswerRepository.findByApplication(application);

		return mapQuestionsWithAnswers(allQuestions, savedAnswers);
	}

	/**
	 * 기타 질문 조회 + 저장된 답변 포함 - 페이지 3용
	 *
	 * @param userId 사용자 ID
	 * @param applicationId 지원서 ID
	 * @return 기타 질문 및 저장된 답변 목록
	 */
	public List<QuestionWithAnswerResponse> getEtcQuestionsWithAnswers(
			Long userId, Long applicationId) {
		Application application = applicationService.getApplicationWithAuth(applicationId, userId);
		Generation activeGeneration = generationService.getActiveGeneration();

		// 기타 질문 조회
		List<Question> etcQuestions =
				questionService.getQuestionsByGenerationAndPartType(activeGeneration, PartType.ETC);

		// 저장된 답변 조회 및 매핑
		List<ApplicationAnswer> savedAnswers =
				applicationAnswerRepository.findByApplication(application);

		return mapQuestionsWithAnswers(etcQuestions, savedAnswers);
	}

	/**
	 * 질문 응답 작성(임시저장)
	 *
	 * @param userId 사용자 ID
	 * @param applicationId 지원서 ID
	 * @param partType 파트 타입 (PM, DE, FE, BE, ETC)
	 * @param requests 질문 응답 요청 목록
	 */
	@Transactional
	public void saveAnswers(
			Long userId, Long applicationId, String partType, List<AnswerRequest> requests) {
		Application application = applicationService.getApplicationWithAuth(applicationId, userId);

		// 이미 제출된 지원서인지 확인
		if (application.isSubmitted()) {
			throw new ApplicationException(ApplicationErrorCode.ALREADY_SUBMITTED);
		}

		// 지원서에 선택한 파트 저장 (ETC가 아닌 경우에만)
		if (!partType.equalsIgnoreCase("ETC") && !partType.equalsIgnoreCase("COMMON")) {
			PartType selectedPart = PartType.valueOf(partType.toUpperCase());
			application.updatePartType(selectedPart);
		}

		// 각 질문에 대한 답변 저장
		for (AnswerRequest request : requests) {
			Question question = questionService.getQuestionById(request.questionId());
			AnswerType answerType = AnswerType.valueOf(request.answerType().toUpperCase());

			// 질문의 answerType과 요청의 answerType이 일치하는지 검증
			if (!question.getAnswerType().equals(answerType)) {
				throw new ApplicationException(ApplicationErrorCode.ANSWER_TYPE_MISMATCH);
			}

			// 기존 답변이 있으면 업데이트, 없으면 새로 생성
			Optional<ApplicationAnswer> existingAnswer =
					applicationAnswerRepository.findByApplicationAndQuestion(application, question);

			if (existingAnswer.isPresent()) {
				// 기존 답변 업데이트
				existingAnswer
						.get()
						.update(
								answerType,
								request.isChecked(),
								request.content(),
								request.fileKey(),
								request.fileUrl());
			} else {
				// 새 답변 생성
				ApplicationAnswer newAnswer =
						ApplicationAnswer.of(
								application,
								question,
								answerType,
								request.isChecked(),
								request.content(),
								request.fileKey(),
								request.fileUrl());
				applicationAnswerRepository.save(newAnswer);
			}
		}
	}
}
