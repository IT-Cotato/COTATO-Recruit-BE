package org.cotato.backend.recruit.presentation.dto.request;

import java.time.LocalDate;
import org.cotato.backend.recruit.domain.application.enums.ApplicationPartType;

public record BasicInfoRequest(
		String name,
		String gender,
		LocalDate birthDate,
		String phoneNumber,
		String university,
		String major,
		Integer completedSemesters,
		Boolean isPrevActivity,
		ApplicationPartType applicationPartType) {}
