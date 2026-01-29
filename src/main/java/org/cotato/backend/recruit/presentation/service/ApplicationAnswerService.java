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
import org.cotato.backend.recruit.presentation.dto.request.PartAnswerRequest;
import org.cotato.backend.recruit.presentation.dto.response.AnswerResponse;
import org.cotato.backend.recruit.presentation.dto.response.PartQuestionResponse;
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
	 * @param questions    질문 목록
	 * @param savedAnswers 저장된 답변 목록
	 * @return 질문과 답변이 매핑된 응답 목록
	 */
	private List<PartQuestionResponse.QuestionWithAnswerResponse> mapQuestionsWithAnswers(
			List<Question> questions, List<ApplicationAnswer> savedAnswers) {
		// 질문 ID를 키로 하는 답변 맵 생성
		Map<Long, ApplicationAnswer> answerMap = savedAnswers.stream()
				.collect(
						Collectors.toMap(
								answer -> answer.getQuestion().getId(), answer -> answer));

		// 질문과 저장된 답변을 함께 반환
		return questions.stream()
				.map(
						q -> {
							ApplicationAnswer savedAnswer = answerMap.get(q.getId());
							AnswerResponse answerResponse = savedAnswer != null ? AnswerResponse.from(savedAnswer)
									: null;

							return PartQuestionResponse.QuestionWithAnswerResponse.of(
									q, answerResponse);
						})
				.collect(Collectors.toList());
	}

	/**
	 * 파트별 질문 조회 (선택한 파트만) + 저장된 답변 포함 - 페이지 2용
	 *
	 * @param userId        사용자 ID
	 * @param applicationId 지원서 ID
	 * @return 질문 및 저장된 답변 목록
	 */
	public PartQuestionResponse getQuestionsWithAnswers(Long userId, Long applicationId) {
		Application application = applicationService.getApplicationWithAuth(applicationId, userId);
		if (application.getIsSubmitted()) {
			throw new PresentationException(PresentationErrorCode.ALREADY_SUBMITTED);
		}

		// Application에서 선택한 파트 가져오기
		if (application.getApplicationPartType() == null) {
			throw new PresentationException(PresentationErrorCode.PART_TYPE_NOT_SELECTED);
		}

		// ApplicationPartType을 QuestionType으로 변환
		QuestionType questionType = application.getApplicationPartType().toQuestionType();

		// 선택한 파트 질문 조회
		List<Question> partQuestions = questionService.getQuestionsByGenerationAndQuestionType(
				application.getGeneration(), questionType);

		// 저장된 답변 조회 및 매핑
		List<ApplicationAnswer> savedAnswers = applicationAnswerRepository.findByApplication(application);

		List<PartQuestionResponse.QuestionWithAnswerResponse> questionList = mapQuestionsWithAnswers(partQuestions,
				savedAnswers);

		return PartQuestionResponse.of(
				questionList, application.getPdfFileUrl(), application.getPdfFileKey());
	}

	/**
	 * 질문 응답 작성(임시저장)
	 *
	 * @param userId        사용자 ID
	 * @param applicationId 지원서 ID
	 * @param requests      질문 응답 요청 목록
	 */
	@Transactional
	public void saveAnswers(
			Long userId,
			Long applicationId,
			List<PartAnswerRequest.AnswerRequest> requests,
			String pdfFileUrl,
			String pdfFileKey) {
		Application application = applicationService.getApplicationWithAuth(applicationId, userId);
		if (application.getIsSubmitted()) {
			throw new PresentationException(PresentationErrorCode.ALREADY_SUBMITTED);
		}

		// 각 질문에 대한 답변 저장
		if (requests != null && !requests.isEmpty()) {
			for (PartAnswerRequest.AnswerRequest request : requests) {
				Question question = questionService.getQuestionById(request.questionId());

				// 기존 답변이 있으면 업데이트, 없으면 새로 생성
				Optional<ApplicationAnswer> existingAnswer = applicationAnswerRepository.findByApplicationAndQuestion(
						application, question);

				if (existingAnswer.isPresent()) {
					// 기존 답변 업데이트
					if (request.content() != null) {
						existingAnswer.get().update(request.content());
					}
				} else {
					// 새 답변 생성
					ApplicationAnswer newAnswer = ApplicationAnswer.of(application, question, request.content());
					applicationAnswerRepository.save(newAnswer);
				}
			}
		}

		// Application PDF 정보 저장
		application.updatePdfInfo(pdfFileUrl, pdfFileKey);
	}
}
