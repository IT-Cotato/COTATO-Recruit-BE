package org.cotato.backend.recruit.admin.service.applicationQuestion;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.applicationQuestion.QuestionUpdateRequest;
import org.cotato.backend.recruit.admin.error.AdminErrorCode;
import org.cotato.backend.recruit.admin.exception.AdminException;
import org.cotato.backend.recruit.admin.service.application.ApplicationAdminService;
import org.cotato.backend.recruit.admin.service.generation.GenerationAdminService;
import org.cotato.backend.recruit.admin.service.question.QuestionAdminService;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.question.entity.Question;
import org.cotato.backend.recruit.domain.question.enums.QuestionType;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionEditAdminService {
	private static final String PORTFOLIO_QUESTION_CONTENT = "(선택) 추가로 제출할 포트폴리오(깃허브,블로그,노션,비핸스 등) 링크를 첨부해주세요. 업로드하실 포트폴리오 양식은 꼭 PDF로 변경 후 제출해주세요!.";
	private static final Integer PORTFOLIO_MAX_LENGTH = -1; // 제한없음 -> -1 처리

	private final GenerationAdminService generationAdminService;
	private final QuestionAdminService questionAdminService;
	private final ApplicationAdminService applicationAdminService;

	// 전체 삭제 후 추가
	@Transactional
	@CacheEvict(value = { "questions", "question" }, allEntries = true)
	public void updateApplicationQuestions(QuestionUpdateRequest request) {
		Generation generation = generationAdminService.getGenerationById(request.generationId());
		QuestionType questionType = request.questionType();

		boolean isApplicationExist = applicationAdminService.isApplicationExistInThisGeneration(generation.getId());

		if (isApplicationExist) {
			throw new AdminException(AdminErrorCode.QUESTION_CANNOT_UPDATE);
		}

		List<Question> existingQuestions = questionAdminService.getQuestionsByGenerationAndQuestionType(
				generation, questionType);
		questionAdminService.deleteAll(existingQuestions);

		List<Question> newQuestions = getNewQuestions(request, generation, questionType);
		questionAdminService.saveAll(newQuestions);

		// 마지막 순서에 포트폴리오 질문 추가
		Integer sequence = newQuestions.size() + 1;
		insertPortfolioQuestion(sequence, questionType, generation);
	}

	private void insertPortfolioQuestion(
			Integer sequence, QuestionType questionType, Generation generation) {
		Question question = Question.builder()
				.generation(generation)
				.sequence(sequence)
				.content(PORTFOLIO_QUESTION_CONTENT)
				.maxLength(PORTFOLIO_MAX_LENGTH)
				.questionType(questionType)
				.build();
		questionAdminService.save(question);
	}

	private List<Question> getNewQuestions(
			QuestionUpdateRequest request, Generation generation, QuestionType questionType) {
		List<Question> newQuestions = request.questions().stream()
				.map(
						element -> Question.builder()
								.generation(generation)
								.sequence(element.sequence())
								.content(element.content())
								.maxLength(element.maxLength())
								.questionType(questionType)
								.build())
				.toList();
		return newQuestions;
	}
}
