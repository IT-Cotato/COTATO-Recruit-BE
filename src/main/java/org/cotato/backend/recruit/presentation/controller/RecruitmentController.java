package org.cotato.backend.recruit.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.cotato.backend.recruit.presentation.dto.response.RecruitmentScheduleResponse;
import org.cotato.backend.recruit.presentation.service.RecruitmentInformationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "모집 정보 API", description = "모집 일정 등 공개 정보 조회 API")
@RestController
@RequestMapping("/api/recruitment")
@RequiredArgsConstructor
public class RecruitmentController {

	private final RecruitmentInformationService recruitmentInformationService;

	@Operation(summary = "모집 일정 조회", description = "현재 모집 중인 기수의 일정 정보를 조회합니다. (인증 불필요)")
	@GetMapping("/schedule")
	public ApiResponse<RecruitmentScheduleResponse> getRecruitmentSchedule() {
		RecruitmentScheduleResponse response =
				recruitmentInformationService.getRecruitmentSchedule();
		return ApiResponse.success(response);
	}
}
