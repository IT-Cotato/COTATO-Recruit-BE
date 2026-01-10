package org.cotato.backend.recruit.admin.dto.request.applicationQuestion;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.cotato.backend.recruit.domain.question.enums.QuestionType;

public record ApplicationQuestionUpdateRequest(
		@NotNull Long generation,
		@NotNull QuestionType questionType,
		@NotNull List<ApplicationQuestionUpdateElement> questions) {}
