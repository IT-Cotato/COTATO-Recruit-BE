package org.cotato.backend.recruit.admin.controller.generation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.generation.generationCreateRequest;
import org.cotato.backend.recruit.admin.service.generation.GenerationAdminService;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "기수 생성 API", description = "기수 생성 관련 API")
@RestController
@RequestMapping("/api/admin/generations")
@RequiredArgsConstructor
public class GenerationCreateController {

	private final GenerationAdminService generationAdminService;

	@Operation(summary = "기수 생성", description = "기수를 생성합니다.")
	@PostMapping
	public ApiResponse<Void> createGeneration(@RequestBody generationCreateRequest request) {
		generationAdminService.createGeneration(request.generationId());
		return ApiResponse.success();
	}
}
