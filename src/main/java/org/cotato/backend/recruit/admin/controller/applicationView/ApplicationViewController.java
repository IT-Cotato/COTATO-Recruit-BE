package org.cotato.backend.recruit.admin.controller.applicationView;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.response.applicationView.AdminApplicationBasicInfoResponse;
import org.cotato.backend.recruit.admin.dto.response.applicationView.AdminApplicationPartQuestionResponse;
import org.cotato.backend.recruit.admin.service.applicationView.ApplicationViewService;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.cotato.backend.recruit.domain.question.enums.PartType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "지원서 조회 API", description = "지원서 조회 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/application")
public class ApplicationViewController {

	private final ApplicationViewService applicationViewService;

	/**
	 * 지원서 기본 정보 조회
	 *
	 * @param applicationId 지원서 ID
	 * @return 지원서 기본 정보
	 */
	@GetMapping("/{applicationId}/basic-info")
	public ApiResponse<AdminApplicationBasicInfoResponse> getBasicInfo(
			@PathVariable Long applicationId) {
		AdminApplicationBasicInfoResponse response =
				applicationViewService.getBasicInfo(applicationId);
		return ApiResponse.success(response);
	}

	/**
	 * 지원서 파트별 질문 조회
	 *
	 * @param applicationId 지원서 ID
	 * @param part 파트 (BE, FE, DE, PM 등)
	 * @return 파트별 질문 및 답변 목록
	 */
	@GetMapping("/{applicationId}/part-questions")
	public ApiResponse<List<AdminApplicationPartQuestionResponse>> getPartQuestions(
			@PathVariable Long applicationId, @RequestParam PartType part) {
		List<AdminApplicationPartQuestionResponse> responses =
				applicationViewService.getPartQuestionsWithAnswers(applicationId, part);
		return ApiResponse.success(responses);
	}

	/**
	 * 지원서 기타 질문 조회
	 *
	 * @param applicationId 지원서 ID
	 * @return 기타 질문 및 답변 목록
	 */
	@GetMapping("/{applicationId}/etc-questions")
	public ApiResponse<List<AdminApplicationPartQuestionResponse>> getEtcQuestions(
			@PathVariable Long applicationId) {
		List<AdminApplicationPartQuestionResponse> responses =
				applicationViewService.getEtcQuestionsWithAnswers(applicationId);
		return ApiResponse.success(responses);
	}
}
