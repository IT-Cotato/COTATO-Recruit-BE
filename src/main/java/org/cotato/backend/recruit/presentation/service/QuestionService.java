package org.cotato.backend.recruit.presentation.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.question.entity.Question;
import org.cotato.backend.recruit.domain.question.enums.QuestionType;
import org.cotato.backend.recruit.domain.question.repository.QuestionRepository;
import org.cotato.backend.recruit.presentation.error.ApplicationErrorCode;
import org.cotato.backend.recruit.presentation.exception.ApplicationException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

	private final QuestionRepository questionRepository;

	/**
	 * 기수와 파트 타입별 질문 조회 (캐시 적용)
	 *
	 * @param generation 기수
	 * @param questionType 파트 타입
	 * @return 질문 목록
	 */
	@Cacheable(value = "questions", key = "#generation.id + '_' + #questionType.name()")
	public List<Question> getQuestionsByGenerationAndQuestionType(
			Generation generation, QuestionType questionType) {

		return questionRepository.findByGenerationAndQuestionTypeOrderBySequenceAsc(
				generation, questionType);
	}

	/**
	 * 질문 ID로 질문 조회 (캐시 적용)
	 *
	 * @param questionId 질문 ID
	 * @return 질문
	 */
	@Cacheable(value = "question", key = "#questionId")
	public Question getQuestionById(Long questionId) {
		return questionRepository
				.findById(questionId)
				.orElseThrow(
						() -> new ApplicationException(ApplicationErrorCode.QUESTION_NOT_FOUND));
	}
}
