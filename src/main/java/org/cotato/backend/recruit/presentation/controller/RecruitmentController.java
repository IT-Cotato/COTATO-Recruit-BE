package org.cotato.backend.recruit.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.cotato.backend.recruit.presentation.dto.request.SubscribeRequest;
import org.cotato.backend.recruit.presentation.dto.response.RecruitmentResponse;
import org.cotato.backend.recruit.presentation.dto.response.RecruitmentScheduleResponse;
import org.cotato.backend.recruit.presentation.dto.response.RecruitmentStatusResponse;
import org.cotato.backend.recruit.presentation.service.RecruitmentService;
import org.cotato.backend.recruit.presentation.service.RecruitmentSubscriberService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "모집 정보 API", description = "모집 일정, FAQ 등 공개 정보 조회 및 알림 구독 API")
@RestController
@RequestMapping("/api/recruitment")
@RequiredArgsConstructor
public class RecruitmentController {

	private final RecruitmentService recruitmentService;
	private final RecruitmentSubscriberService recruitmentSubscriberService;

	@Operation(
			summary = "모집 일정 조회",
			description = "현재 모집 중인 기수의 일정 정보를 조회합니다. 지원서 화면에 노출됩니다. (인증 불필요)")
	@GetMapping("/schedule")
	public ApiResponse<RecruitmentScheduleResponse> getRecruitmentSchedule() {
		RecruitmentScheduleResponse response = recruitmentService.getRecruitmentSchedule();
		return ApiResponse.success(response);
	}

	@Operation(
			summary = "모집 공고 조회",
			description = "모집 공고 페이지의 기수 일정 정보, 파트별 정보, 모집 일정 등을 조회합니다. (인증 불필요)")
	@GetMapping
	public ApiResponse<RecruitmentResponse> getRecruitment() {
		return ApiResponse.success(recruitmentService.getRecruitmentData());
	}

	@Operation(
			summary = "모집 활성화 상태 조회",
			description =
					"현재 모집이 활성화되어 있는지 확인합니다. (인증 불필요)\n\n"
							+ "- isActive: true인 경우 모집 진행 중, false인 경우 모집 미진행\n"
							+ "- generationId: 모집 중인 기수 번호 (모집 미진행 시 null)")
	@GetMapping("/status")
	public ApiResponse<RecruitmentStatusResponse> getRecruitmentStatus() {
		RecruitmentStatusResponse response = recruitmentService.checkRecruitmentStatus();
		return ApiResponse.success(response);
	}

	@Operation(
			summary = "모집 알림 구독 신청",
			description = "모집이 시작될 때 이메일로 알림을 받을 수 있도록 구독 신청합니다. (인증 불필요)")
	@PostMapping("/subscribe")
	public ApiResponse<Void> subscribe(@Valid @RequestBody SubscribeRequest request) {
		recruitmentSubscriberService.subscribe(request.email());
		return ApiResponse.success();
	}
}
