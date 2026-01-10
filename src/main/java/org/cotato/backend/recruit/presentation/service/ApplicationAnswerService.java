package org.cotato.backend.recruit.presentation.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.entity.ApplicationAnswer;
import org.cotato.backend.recruit.domain.application.repository.ApplicationAnswerRepository;
import org.cotato.backend.recruit.domain.question.entity.Question;
import org.cotato.backend.recruit.domain.question.enums.QuestionType;
import org.cotato.backend.recruit.presentation.dto.request.AnswerRequest;
import org.cotato.backend.recruit.presentation.dto.response.AnswerResponse;
import org.cotato.backend.recruit.presentation.dto.response.QuestionWithAnswerResponse;
import org.cotato.backend.recruit.presentation.error.PresentationErrorCode;
import org.cotato.backend.recruit.presentation.exception.PresentationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationAnswerService {

	private final ApplicationAnswerRepository applicationAnswerRepository;
	private final QuestionService questionService;
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
									savedAnswer != null ? AnswerResponse.from(savedAnswer) : null;

							return QuestionWithAnswerResponse.of(q, answerResponse);
						})
				.collect(Collectors.toList());
	}

	/**
	 * 파트별 질문 조회 (선택한 파트만) + 저장된 답변 포함 - 페이지 2용
	 *
	 * @param userId 사용자 ID
	 * @param applicationId 지원서 ID
	 * @return 질문 및 저장된 답변 목록
	 */
	public List<QuestionWithAnswerResponse> getQuestionsWithAnswers(
			Long userId, Long applicationId) {
		Application application = applicationService.getApplicationWithAuth(applicationId, userId);

		// Application에서 선택한 파트 가져오기
		if (application.getApplicationPartType() == null) {
			throw new PresentationException(PresentationErrorCode.PART_TYPE_NOT_SELECTED);
		}

		// ApplicationPartType을 QuestionType으로 변환
		QuestionType questionType = application.getApplicationPartType().toQuestionType();

		// 선택한 파트 질문 조회
		List<Question> partQuestions =
				questionService.getQuestionsByGenerationAndQuestionType(
						application.getGeneration(), questionType);

		// 저장된 답변 조회 및 매핑
		List<ApplicationAnswer> savedAnswers =
				applicationAnswerRepository.findByApplication(application);

		return mapQuestionsWithAnswers(partQuestions, savedAnswers);
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

		// 기타 질문 조회
		List<Question> etcQuestions =
				questionService.getQuestionsByGenerationAndQuestionType(
						application.getGeneration(), QuestionType.ETC);

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
	 * @param requests 질문 응답 요청 목록
	 */
	@Transactional
	public void saveAnswers(Long userId, Long applicationId, List<AnswerRequest> requests) {
		Application application = applicationService.getApplicationWithAuth(applicationId, userId);

		// 각 질문에 대한 답변 저장
		for (AnswerRequest request : requests) {
			Question question = questionService.getQuestionById(request.questionId());

			// 질문의 answerType과 요청의 answerType이 일치하는지 검증
			if (!question.getAnswerType().equals(request.answerType())) {
				throw new PresentationException(PresentationErrorCode.ANSWER_TYPE_MISMATCH);
			}

			// 기존 답변이 있으면 업데이트, 없으면 새로 생성
			Optional<ApplicationAnswer> existingAnswer =
					applicationAnswerRepository.findByApplicationAndQuestion(application, question);

			if (existingAnswer.isPresent()) {
				// 기존 답변 업데이트
				existingAnswer
						.get()
						.update(
								request.answerType(),
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
								request.answerType(),
								request.isChecked(),
								request.content(),
								request.fileKey(),
								request.fileUrl());
				applicationAnswerRepository.save(newAnswer);
			}
		}
	}
}
