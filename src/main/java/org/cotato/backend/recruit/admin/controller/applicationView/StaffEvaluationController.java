package org.cotato.backend.recruit.admin.controller.applicationView;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.staffEvaluation.CreateStaffEvaluationRequest;
import org.cotato.backend.recruit.admin.dto.response.staffEvaluation.StaffEvaluationResponse;
import org.cotato.backend.recruit.admin.service.staffEvaluation.StaffEvaluationService;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.cotato.backend.recruit.domain.evaluation.enums.EvaluatorType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "운영진 평가 관리 API", description = "지원서별 운영진 평가 조회 및 생성/수정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/application/{applicationId}/evaluation")
public class StaffEvaluationController {

	private final StaffEvaluationService staffEvaluationService;

	/**
	 * 운영진 평가를 조회합니다.
	 *
	 * @param applicationId 지원서 ID
	 * @param evaluatorType 평가자 타입 (STAFF1, STAFF2, STAFF3, STAFF4)
	 * @return 평가 내용
	 */
	@GetMapping
	public ApiResponse<StaffEvaluationResponse> getEvaluation(
			@PathVariable("applicationId") Long applicationId,
			@RequestParam("evaluatorType") EvaluatorType evaluatorType) {
		StaffEvaluationResponse response =
				staffEvaluationService.getEvaluation(applicationId, evaluatorType);
		return ApiResponse.success(response);
	}

	/**
	 * 운영진 평가를 생성하거나 수정합니다.
	 *
	 * @param applicationId 지원서 ID
	 * @param request 평가 정보 (평가자 타입, 내용)
	 * @return 성공 응답
	 */
	@PostMapping
	public ApiResponse<Void> createEvaluation(
			@PathVariable("applicationId") Long applicationId,
			@Valid @RequestBody CreateStaffEvaluationRequest request) {
		staffEvaluationService.createEvaluation(applicationId, request);
		return ApiResponse.success();
	}
}
