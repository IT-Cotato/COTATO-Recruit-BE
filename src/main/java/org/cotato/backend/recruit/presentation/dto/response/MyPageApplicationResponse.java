package org.cotato.backend.recruit.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.cotato.backend.recruit.domain.application.entity.Application;

@Schema(description = "마이페이지 지원 현황 응답")
public record MyPageApplicationResponse(
        @Schema(description = "지원서 ID", example = "1") Long applicationId,
        @Schema(description = "기수", example = "10") Integer generationNumber,
        @Schema(description = "지원 파트", example = "BACKEND") String part,
        @Schema(description = "지원 상태", example = "지원완료") String status) {

    public static MyPageApplicationResponse of(Application application) {
        String status = "지원완료";
        String part = application.getApplicationPartType() != null
                ? application.getApplicationPartType().name()
                : null;

        return new MyPageApplicationResponse(
                application.getId(), application.getGeneration().getId().intValue(), part, status);
    }
}
