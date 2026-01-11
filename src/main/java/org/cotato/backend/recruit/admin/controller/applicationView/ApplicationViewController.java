package org.cotato.backend.recruit.admin.controller.applicationView;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.response.applicationView.AdminApplicationBasicInfoResponse;
import org.cotato.backend.recruit.admin.dto.response.applicationView.AdminApplicationEtcQuestionsResponse;
import org.cotato.backend.recruit.admin.dto.response.applicationView.AdminApplicationPartQuestionResponse;
import org.cotato.backend.recruit.admin.service.applicationView.ApplicationViewService;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.cotato.backend.recruit.domain.question.enums.QuestionType;
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
	 * @return 파트별 질문 및 답변 목록
	 */
	@GetMapping("/{applicationId}/part-questions")
	public ApiResponse<AdminApplicationPartQuestionResponse> getPartQuestions(
			@PathVariable Long applicationId, @RequestParam QuestionType questionType) {
		AdminApplicationPartQuestionResponse responses =
				applicationViewService.getPartQuestionsWithAnswers(applicationId, questionType);
		return ApiResponse.success(responses);
	}

	/**
	 * 지원서 기타 질문 조회
	 *
	 * @param applicationId 지원서 ID
	 * @return 기타 질문 및 답변
	 */
	@GetMapping("/{applicationId}/etc-questions")
	public ApiResponse<AdminApplicationEtcQuestionsResponse> getEtcQuestions(
			@PathVariable Long applicationId) {
		AdminApplicationEtcQuestionsResponse response =
				applicationViewService.getEtcQuestionsWithAnswers(applicationId);
		return ApiResponse.success(response);
	}
}
