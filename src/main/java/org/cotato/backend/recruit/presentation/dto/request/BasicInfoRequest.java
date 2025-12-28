package org.cotato.backend.recruit.presentation.dto.request;

import java.time.LocalDate;

public record BasicInfoRequest(
		String name,
		String gender,
		LocalDate birthDate,
		String phoneNumber,
		String university,
		String major,
		Integer completedSemesters,
		Boolean isPrevActivity) {}
