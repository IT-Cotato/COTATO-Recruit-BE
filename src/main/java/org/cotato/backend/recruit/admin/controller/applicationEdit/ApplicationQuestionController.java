package org.cotato.backend.recruit.admin.controller.applicationEdit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.applicationQuestion.QuestionUpdateRequest;
import org.cotato.backend.recruit.admin.dto.response.applicationQuestion.ApplicationQuestionResponse;
import org.cotato.backend.recruit.admin.service.applicationQuestion.QuestionEditAdminService;
import org.cotato.backend.recruit.admin.service.question.QuestionAdminService;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.cotato.backend.recruit.domain.question.enums.QuestionType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "지원서 질문 관리 API", description = "지원서 질문 조회 및 수정(관리자) API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/application-questions")
public class ApplicationQuestionController {

	private final QuestionEditAdminService questionEditAdminService;
	private final QuestionAdminService questionAdminService;

	/**
	 * 지원서 질문 조회
	 *
	 * @param generationId 기수
	 * @param questionType 파트
	 * @return 지원서 질문 목록
	 */
	@Operation(summary = "지원서 질문 조회", description = "해당 기수 및 파트의 지원서 질문을 조회합니다.")
	@GetMapping
	public ApiResponse<List<ApplicationQuestionResponse>> getQuestions(
			@RequestParam("generationId") Long generationId,
			@RequestParam("questionType") QuestionType questionType) {
		List<ApplicationQuestionResponse> response =
				questionAdminService.getApplicationQuestions(generationId, questionType);
		return ApiResponse.success(response);
	}

	/**
	 * 지원서 질문 수정
	 *
	 * @param request 지원서 질문 수정 요청 정보
	 * @return 성공 응답
	 */
	@Operation(summary = "지원서 질문 수정", description = "해당 기수의 지원서 공통 질문을 수정합니다. (기존 질문 삭제 후 재등록)")
	@PostMapping
	public ApiResponse<Void> updateQuestions(@Valid @RequestBody QuestionUpdateRequest request) {
		questionEditAdminService.updateApplicationQuestions(request);
		return ApiResponse.success();
	}
}
