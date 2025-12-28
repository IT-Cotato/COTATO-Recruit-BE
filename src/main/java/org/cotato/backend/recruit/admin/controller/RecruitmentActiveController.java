package org.cotato.backend.recruit.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.ActivationRequest;
import org.cotato.backend.recruit.admin.service.RecruitmentActiveService;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "모집 활성화 API", description = "모집 기간 활성화 및 조회 관련 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class RecruitmentActiveController {

	private final RecruitmentActiveService recruitmentActiveService;

	@Operation(summary = "모집 활성화", description = "해당 기수의 모집을 활성화하고 시작/종료일을 설정합니다.")
	@PostMapping("/recruitment-activation")
	public ApiResponse<Void> activateRecruitment(@RequestBody ActivationRequest request) {
		recruitmentActiveService.activateRecruitment(
				request.getGeneration(), request.getStartDate(), request.getEndDate());
		return ApiResponse.success();
	}

	// @Operation(summary = "모집 활성화 조회", description = "해당 기수의 모집 활성화 정보를 조회합니다.")
	// // query parameter
	// @GetMapping("/recruitment-activation")
	// public ApiResponse<ActivationResponse> getRecruitmentActivation(
	// @RequestParam(name = "generation") Long generationId) {
	// ActivationResponse response =
	// recruitmentActiveService.getRecruitmentActivation(generationId);
	// return ApiResponse.success(response);
	// }
}
