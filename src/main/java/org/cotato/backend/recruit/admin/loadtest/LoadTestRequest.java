package org.cotato.backend.recruit.admin.loadtest;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 부하 테스트 요청 DTO
 */
@Schema(description = "부하 테스트 요청")
public record LoadTestRequest(
        @Schema(description = "테스트용 콘텐츠 (약 2000자)", example = "테스트 콘텐츠...") String content) {
}
