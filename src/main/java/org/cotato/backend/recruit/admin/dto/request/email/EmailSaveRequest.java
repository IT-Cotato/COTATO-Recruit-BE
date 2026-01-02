package org.cotato.backend.recruit.admin.dto.request.email;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.cotato.backend.recruit.domain.email.enums.TemplateType;

@Schema(description = "메일 내용 저장 요청")
public record EmailSaveRequest(
		@Schema(description = "템플릿 타입 (PASS, FAIL, PRELIMINARY)", example = "PASS")
				@NotNull(message = "템플릿 타입은 필수입니다.")
				TemplateType templateType,
		@Schema(description = "기수 ID (미입력시 현재 모집 중인 기수)", example = "1") Long generationId,
		@Schema(description = "메일 내용", example = "합격을 축하드립니다!") @NotBlank(message = "메일 내용은 필수입니다.")
				String content) {}
