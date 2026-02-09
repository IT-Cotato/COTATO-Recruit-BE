package org.cotato.backend.recruit.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.auth.dto.CustomUserDetails;
import org.cotato.backend.recruit.common.annotation.MonitorFailure;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.cotato.backend.recruit.presentation.dto.response.BasicInfoResponse;
import org.cotato.backend.recruit.presentation.dto.response.EtcAnswerResponse;
import org.cotato.backend.recruit.presentation.dto.response.MyPageApplicationResponse;
import org.cotato.backend.recruit.presentation.dto.response.PartQuestionResponse;
import org.cotato.backend.recruit.presentation.service.SubmittedApplicationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "제출된 지원서 조회 API", description = "지원자가 제출한 지원서를 조회하는 API")
@RestController
@RequestMapping("/api/submitted-applications")
@RequiredArgsConstructor
public class SubmittedApplicationViewController {

	private final SubmittedApplicationService submittedApplicationService;

	@Operation(summary = "마이페이지 지원 현황 조회", description = "마이페이지에서 사용자의 지원 현황을 조회합니다.")
	@GetMapping("/mypage")
	@MonitorFailure(apiName = "마이페이지 지원 현황 조회")
	public ApiResponse<List<MyPageApplicationResponse>> getMyApplications(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
		List<MyPageApplicationResponse> response =
				submittedApplicationService.getMyApplications(userDetails.getUserId());
		return ApiResponse.success(response);
	}

	@Operation(
			summary = "제출된 지원서 기본 인적사항 조회",
			description = "지원자가 제출한 지원서의 기본 인적사항을 조회합니다. 본인의 지원서만 조회할 수 있습니다.")
	@GetMapping("/{applicationId}/basic-info")
	@MonitorFailure(apiName = "제출된 지원서 기본 인적사항 조회")
	public ApiResponse<BasicInfoResponse> getBasicInfo(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
			@Parameter(description = "지원서 ID", required = true) @PathVariable("applicationId")
					Long applicationId) {
		BasicInfoResponse response =
				submittedApplicationService.getBasicInfo(userDetails.getUserId(), applicationId);
		return ApiResponse.success(response);
	}

	@Operation(
			summary = "제출된 지원서 파트별 질문 및 답변 조회",
			description = "지원자가 제출한 지원서의 파트별 질문과 답변을 조회합니다. 본인의 지원서만 조회할 수 있습니다.")
	@GetMapping("/{applicationId}/part-questions")
	@MonitorFailure(apiName = "제출된 지원서 파트별 질문 및 답변 조회")
	public ApiResponse<PartQuestionResponse> getPartQuestionsWithAnswers(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
			@Parameter(description = "지원서 ID", required = true) @PathVariable("applicationId")
					Long applicationId) {
		PartQuestionResponse response =
				submittedApplicationService.getPartQuestionsWithAnswers(
						userDetails.getUserId(), applicationId);
		return ApiResponse.success(response);
	}

	@Operation(
			summary = "제출된 지원서 기타 정보 조회",
			description =
					"지원자가 제출한 지원서의 기타 정보(알게 된 경로, 병행 활동, 동의사항 등)를 조회합니다. 본인의 지원서만 조회할 수 있습니다.")
	@GetMapping("/{applicationId}/etc-info")
	@MonitorFailure(apiName = "제출된 지원서 기타 정보 조회")
	public ApiResponse<EtcAnswerResponse> getEtcInfo(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
			@Parameter(description = "지원서 ID", required = true) @PathVariable("applicationId")
					Long applicationId) {
		EtcAnswerResponse response =
				submittedApplicationService.getEtcInfo(userDetails.getUserId(), applicationId);
		return ApiResponse.success(response);
	}
}
