package org.cotato.backend.recruit.admin.service.questionAdmin;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.question.entity.Question;
import org.cotato.backend.recruit.domain.question.enums.PartType;
import org.cotato.backend.recruit.domain.question.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionAdminService {
	private final QuestionRepository questionRepository;

	public List<Question> getQuestionsByGenerationAndPartType(
			Generation generation, PartType partType) {
		return questionRepository.findByGenerationAndPartType(generation, partType);
	}
}
