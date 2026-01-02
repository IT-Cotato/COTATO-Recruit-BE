package org.cotato.backend.recruit.admin.dto.request.applicationQuestion;

import java.util.List;

public record ApplicationQuestionUpdateRequest(
		Long generation, String partType, List<ApplicationQuestionUpdateElement> questions) {}
