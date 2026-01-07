package org.cotato.backend.recruit.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.auth.dto.CustomUserDetails;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.cotato.backend.recruit.presentation.dto.request.AnswerRequest;
import org.cotato.backend.recruit.presentation.dto.response.ApplicationStartResponse;
import org.cotato.backend.recruit.presentation.dto.response.QuestionWithAnswerResponse;
import org.cotato.backend.recruit.presentation.service.ApplicationAnswerService;
import org.cotato.backend.recruit.presentation.service.ApplicationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "지원서 화면 API", description = "사용자 지원서 작성 화면 API")
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

	private final ApplicationService applicationService;
	private final ApplicationAnswerService applicationAnswerService;

	@Operation(
			summary = "지원서 시작 (지원하기 버튼)",
			description = "지원하기 버튼 클릭 시 호출됩니다. 이미 해당 기수에 지원서가 있으면 기존 지원서 ID 반환, 없으면 새로 생성합니다.")
	@PostMapping("/start")
	public ApiResponse<ApplicationStartResponse> startApplication(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
		ApplicationStartResponse response =
				applicationService.startApplication(userDetails.getUserId());
		return ApiResponse.success(response);
	}

	@Operation(
			summary = "파트별 질문 + 저장된 답변 조회 (페이지 2)",
			description = "공통 + 선택한 파트 질문을 저장된 답변과 함께 조회합니다. (페이지 2 진입 시)")
	@GetMapping("/{applicationId}/part-questions")
	public ApiResponse<List<QuestionWithAnswerResponse>> getQuestionsWithAnswers(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
			@Parameter(description = "지원서 ID", required = true) @PathVariable Long applicationId,
			@Parameter(description = "파트 타입 (PM, DE, FE, BE)", required = true) @RequestParam
					String partType) {
		List<QuestionWithAnswerResponse> response =
				applicationAnswerService.getQuestionsWithAnswers(
						userDetails.getUserId(), applicationId, partType);
		return ApiResponse.success(response);
	}

	@Operation(
			summary = "기타 질문 + 저장된 답변 조회 (페이지 3)",
			description = "기타(ETC) 질문을 저장된 답변과 함께 조회합니다. (페이지 3 진입 시)")
	@GetMapping("/{applicationId}/etc-questions")
	public ApiResponse<List<QuestionWithAnswerResponse>> getEtcQuestionsWithAnswers(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
			@Parameter(description = "지원서 ID", required = true) @PathVariable Long applicationId) {
		List<QuestionWithAnswerResponse> response =
				applicationAnswerService.getEtcQuestionsWithAnswers(
						userDetails.getUserId(), applicationId);
		return ApiResponse.success(response);
	}

	@Operation(
			summary = "질문 응답 작성(임시저장)",
			description =
					"지원서 질문에 대한 응답을 작성하고 임시 저장합니다. 임시 저장 또는 다음 버튼 클릭 시 "
							+ "해당 페이지의 질문 ID와 답변을 전송해야 합니다.")
	@PostMapping("/{applicationId}/answers")
	public ApiResponse<Void> saveAnswers(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
			@Parameter(description = "지원서 ID", required = true) @PathVariable Long applicationId,
			@Parameter(description = "파트 타입 (PM, DE, FE, BE, ETC)", required = true) @RequestParam
					String partType,
			@Parameter(description = "질문 응답 목록", required = true) @Valid @RequestBody
					List<AnswerRequest> requests) {
		applicationAnswerService.saveAnswers(
				userDetails.getUserId(), applicationId, partType, requests);
		return ApiResponse.success();
	}

	@Operation(
			summary = "지원서 최종 제출",
			description = "작성한 지원서를 최종 제출합니다. 최종 제출 하기 전에 임시저장을 먼저 해야 합니다.")
	@PostMapping("/{applicationId}/submit")
	public ApiResponse<Void> submitApplication(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
			@Parameter(description = "지원서 ID", required = true) @PathVariable Long applicationId) {
		applicationService.submitApplication(userDetails.getUserId(), applicationId);
		return ApiResponse.success();
	}
}
