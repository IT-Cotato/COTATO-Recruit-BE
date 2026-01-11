package org.cotato.backend.recruit.admin.service.question;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.response.applicationQuestion.ApplicationQuestionResponse;
import org.cotato.backend.recruit.admin.service.generation.GenerationAdminService;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.question.entity.Question;
import org.cotato.backend.recruit.domain.question.enums.QuestionType;
import org.cotato.backend.recruit.domain.question.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionAdminService {
	private final QuestionRepository questionRepository;
	private final GenerationAdminService generationAdminService;

	public List<Question> getQuestionsByGenerationAndQuestionType(
			Generation generation, QuestionType questionType) {
		return questionRepository.findByGenerationAndQuestionType(generation, questionType);
	}

	private List<Question> getQuestionsByGenerationAndQuestionTypeOrderBySequenceAsc(
			Generation generation, QuestionType questionType) {
		return questionRepository.findByGenerationAndQuestionTypeOrderBySequenceAsc(
				generation, questionType);
	}

	public List<ApplicationQuestionResponse> getApplicationQuestions(
			Long generationId, QuestionType questionType) {
		Generation generation = generationAdminService.getGenerationById(generationId);

		List<Question> questions =
				getQuestionsByGenerationAndQuestionTypeOrderBySequenceAsc(generation, questionType);

		return questions.stream().map(ApplicationQuestionResponse::from).toList();
	}

	@Transactional
	public void deleteAll(List<Question> questions) {
		questionRepository.deleteAll(questions);
	}

	@Transactional
	public void saveAll(List<Question> questions) {
		questionRepository.saveAll(questions);
	}

	@Transactional
	public void save(Question question) {
		questionRepository.save(question);
	}
}
