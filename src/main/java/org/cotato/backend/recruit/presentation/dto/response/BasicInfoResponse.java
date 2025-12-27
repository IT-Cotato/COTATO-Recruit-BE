package org.cotato.backend.recruit.presentation.dto.response;

import java.time.LocalDate;

public record BasicInfoResponse(
		Long applicationId,
		String name,
		String gender,
		LocalDate birthDate,
		String phoneNumber,
		String university,
		String major,
		Integer completedSemesters,
		Boolean isPrevActivity) {}
