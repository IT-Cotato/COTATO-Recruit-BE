package org.cotato.backend.recruit.presentation.dto.request;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.cotato.backend.recruit.domain.application.enums.ApplicationPartType;

public record BasicInfoRequest(
		@Size(max = 10, message = "이름은 최대 10글자까지 입력 가능합니다.") String name,
		String gender,
		LocalDate birthDate,
		String phoneNumber,
		@Size(max = 20, message = "학교명은 최대 20글자까지 입력 가능합니다.") String university,
		String major,
		Integer completedSemesters,
		Boolean isPrevActivity,
		Boolean isEnrolled,
		ApplicationPartType applicationPartType) {}
