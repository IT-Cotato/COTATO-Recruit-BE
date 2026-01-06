package org.cotato.backend.recruit.admin.service.applicationQuestion;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.applicationQuestion.ApplicationQuestionUpdateRequest;
import org.cotato.backend.recruit.admin.dto.response.applicationQuestion.ApplicationQuestionResponse;
import org.cotato.backend.recruit.admin.service.generation.GenerationAdminService;
import org.cotato.backend.recruit.admin.service.question.QuestionAdminService;
import org.cotato.backend.recruit.domain.application.enums.AnswerType;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.question.entity.Question;
import org.cotato.backend.recruit.domain.question.enums.PartType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationQuestionAdminService {

	private final GenerationAdminService generationAdminService;
	private final QuestionAdminService questionAdminService;

	public List<ApplicationQuestionResponse> getApplicationQuestions(
			Long generationId, String partType) {
		Generation generation = generationAdminService.getGenerationById(generationId);
		PartType type = PartType.fromString(partType);

		List<Question> questions =
				questionAdminService.getQuestionsByGenerationAndPartTypeOrderBySequenceAsc(
						generation, type);

		return questions.stream().map(ApplicationQuestionResponse::from).toList();
	}

	@Transactional
	public void updateApplicationQuestions(ApplicationQuestionUpdateRequest request) {
		Generation generation = generationAdminService.getGenerationById(request.generation());
		PartType partType = PartType.fromString(request.partType());

		List<Question> existingQuestions =
				questionAdminService.getQuestionsByGenerationAndPartType(generation, partType);
		questionAdminService.deleteAll(existingQuestions);

		List<Question> newQuestions = getNewQuestions(request, generation, partType);
		questionAdminService.saveAll(newQuestions);
	}

	private List<Question> getNewQuestions(
			ApplicationQuestionUpdateRequest request, Generation generation, PartType partType) {
		List<Question> newQuestions =
				request.questions().stream()
						.map(
								element ->
										Question.builder()
												.generation(generation)
												.sequence(element.sequence())
												.content(element.content())
												.maxByte(element.maxByte())
												.partType(partType)
												.answerType(AnswerType.TEXT)
												.build())
						.toList();
		return newQuestions;
	}
}
