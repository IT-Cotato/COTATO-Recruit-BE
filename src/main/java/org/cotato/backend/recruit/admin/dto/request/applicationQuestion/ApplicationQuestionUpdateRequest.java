package org.cotato.backend.recruit.admin.dto.request.applicationQuestion;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ApplicationQuestionUpdateRequest(
		@NotNull Long generation,
		@NotNull String partType,
		@NotNull List<ApplicationQuestionUpdateElement> questions) {}
