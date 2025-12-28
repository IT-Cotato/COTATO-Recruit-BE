package org.cotato.backend.recruit.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.auth.dto.CustomUserDetails;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.cotato.backend.recruit.presentation.dto.request.BasicInfoRequest;
import org.cotato.backend.recruit.presentation.dto.response.BasicInfoResponse;
import org.cotato.backend.recruit.presentation.service.BasicInfoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "기본 인적사항 API", description = "지원자의 기본 인적사항 조회 및 작성 API")
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class BasicInfoController {

	private final BasicInfoService basicInfoService;

	@Operation(summary = "기본 인적사항 조회", description = "저장된 기본 인적사항을 불러옵니다. (페이지 1 진입 시)")
	@GetMapping("/{applicationId}/basic-info")
	public ApiResponse<BasicInfoResponse> getBasicInfo(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
			@Parameter(description = "지원서 ID", required = true) @PathVariable Long applicationId) {
		BasicInfoResponse response =
				basicInfoService.getBasicInfo(userDetails.getUserId(), applicationId);
		return ApiResponse.success(response);
	}

	@Operation(summary = "기본 인적사항 작성(임시저장)", description = "지원자의 기본 인적사항을 작성하고 임시 저장합니다.")
	@PostMapping("/{applicationId}/basic-info")
	public ApiResponse<Void> saveBasicInfo(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
			@Parameter(description = "지원서 ID", required = true) @PathVariable Long applicationId,
			@Parameter(description = "기본 인적사항", required = true) @Valid @RequestBody
					BasicInfoRequest request) {
		basicInfoService.saveBasicInfo(userDetails.getUserId(), applicationId, request);
		return ApiResponse.success();
	}
}
