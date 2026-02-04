package org.cotato.backend.recruit.admin.controller.generation;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.response.generation.GenerationElementResponse;
import org.cotato.backend.recruit.admin.service.generation.GenerationAdminService;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "기수 목록 조회 API", description = "기수 목록 조회 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/generations")
public class GenerationListController {

	private final GenerationAdminService generationAdminService;

	/**
	 * 기수 목록 조회
	 *
	 * @return 기수 목록 (내림차순)
	 */
	@GetMapping
	public ApiResponse<List<GenerationElementResponse>> getGenerations() {
		List<GenerationElementResponse> generations = generationAdminService.getAllGenerations();
		return ApiResponse.success(generations);
	}
}
