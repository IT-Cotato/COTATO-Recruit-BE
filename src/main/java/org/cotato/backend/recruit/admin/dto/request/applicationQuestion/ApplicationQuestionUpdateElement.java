package org.cotato.backend.recruit.admin.dto.request.applicationQuestion;

public record ApplicationQuestionUpdateElement(
		Integer sequence, String content, Integer maxLength) {}
