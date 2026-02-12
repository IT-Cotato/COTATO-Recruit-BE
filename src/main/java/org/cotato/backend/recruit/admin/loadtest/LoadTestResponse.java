package org.cotato.backend.recruit.admin.loadtest;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 부하 테스트 응답 DTO
 */
@Schema(description = "부하 테스트 응답")
public record LoadTestResponse(
        @Schema(description = "저장된 데이터 ID", example = "1") Long id,

        @Schema(description = "콘텐츠 길이", example = "2000") Integer contentLength,

        @Schema(description = "처리 결과 메시지", example = "Load test completed successfully") String message) {
    public static LoadTestResponse of(Long id, Integer contentLength) {
        return new LoadTestResponse(
                id,
                contentLength,
                "Load test completed successfully");
    }
}
