package org.cotato.backend.recruit.admin.controller.recruitmentActive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.recruitmentActive.ActivationRequest;
import org.cotato.backend.recruit.admin.dto.request.recruitmentActive.DeactivationReqeust;
import org.cotato.backend.recruit.admin.service.recruitmentActive.RecruitmentActiveService;
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

	/**
	 * 모집 활성화
	 *
	 * @param request 모집 활성화 요청 정보
	 * @return 성공 응답
	 */
	@Operation(summary = "모집 활성화", description = "해당 기수의 모집을 활성화하고 시작/종료일을 설정합니다.")
	@PostMapping("/recruitment-activation")
	public ApiResponse<Void> activateRecruitment(@Valid @RequestBody ActivationRequest request) {
		recruitmentActiveService.activateRecruitment(
				request.generation(),
				request.isAdditionalRecruitmentActive(),
				request.startDate(),
				request.endDate());
		return ApiResponse.success();
	}

	@Operation(summary = "모집 종료", description = "해당 기수의 모집을 종료합니다.")
	@PostMapping("/recruitment-deactivation")
	public ApiResponse<Void> deactivateRecruitment(
			@Valid @RequestBody DeactivationReqeust request) {
		recruitmentActiveService.deactivateRecruitment(request.generation());
		return ApiResponse.success();
	}
}
