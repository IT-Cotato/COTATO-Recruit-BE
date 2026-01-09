package org.cotato.backend.recruit.presentation.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.question.entity.Question;
import org.cotato.backend.recruit.domain.question.enums.PartType;
import org.cotato.backend.recruit.domain.question.repository.QuestionRepository;
import org.cotato.backend.recruit.presentation.error.PresentationErrorCode;
import org.cotato.backend.recruit.presentation.exception.PresentationException;
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
	 * @param partType 파트 타입
	 * @return 질문 목록
	 */
	@Cacheable(value = "questions", key = "#generation.id + '_' + #partType.name()")
	public List<Question> getQuestionsByGenerationAndPartType(
			Generation generation, PartType partType) {

		return questionRepository.findByGenerationAndPartTypeOrderBySequenceAsc(
				generation, partType);
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
						() -> new PresentationException(PresentationErrorCode.QUESTION_NOT_FOUND));
	}
}
