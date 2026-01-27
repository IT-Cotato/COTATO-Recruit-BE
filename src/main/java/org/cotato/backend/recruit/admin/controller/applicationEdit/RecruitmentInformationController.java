package org.cotato.backend.recruit.admin.controller.applicationEdit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.recruitmentInformation.RecruitmentInformationUpdateRequest;
import org.cotato.backend.recruit.admin.dto.response.recruitmentInformation.RecruitmentInformationResponse;
import org.cotato.backend.recruit.admin.service.recruitmentInformation.RecruitmentInformationAdminService;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "모집 일정 관리 API", description = "모집 일정 조회 및 수정(관리자) API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/recruitment-informations")
public class RecruitmentInformationController {

	private final RecruitmentInformationAdminService recruitmentInformationAdminService;

	/**
	 * 모집 일정 조회
	 *
	 * @param generation 기수
	 * @return 모집 일정 정보
	 */
	@Operation(summary = "모집 일정 조회", description = "해당 기수의 모집 일정을 조회합니다.")
	@GetMapping
	public ApiResponse<RecruitmentInformationResponse> getRecruitmentInformation(
			@RequestParam("generationId") Long generationId) {
		RecruitmentInformationResponse response =
				recruitmentInformationAdminService.getRecruitmentInformation(generationId);
		return ApiResponse.success(response);
	}

	/**
	 * 모집 일정 수정
	 *
	 * @param request 모집 일정 수정 요청 정보
	 * @return 성공 응답
	 */
	@Operation(summary = "모집 일정 수정", description = "해당 기수의 모집 일정을 수정합니다.")
	@PostMapping
	public ApiResponse<Void> updateRecruitmentInformation(
			@Valid @RequestBody RecruitmentInformationUpdateRequest request) {
		recruitmentInformationAdminService.updateRecruitmentInformation(request);
		return ApiResponse.success();
	}
}
