package org.cotato.backend.recruit.admin.dto.response.applicationView;

import lombok.Builder;
import org.cotato.backend.recruit.domain.application.enums.PassStatus;
import org.cotato.backend.recruit.domain.question.enums.PartType;

@Builder
public record ApplicationElementResponse(
		Long applicationId,
		String name,
		String gender,
		PartType part,
		String university,
		String phoneNumber,
		PassStatus passStatus) {}
