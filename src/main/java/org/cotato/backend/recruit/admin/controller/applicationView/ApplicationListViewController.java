package org.cotato.backend.recruit.admin.controller.applicationView;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.applicationView.ApplicationListRequest;
import org.cotato.backend.recruit.admin.dto.response.applicationView.AdminApplicationsResponse;
import org.cotato.backend.recruit.admin.service.applicationView.ApplicationViewListService;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "지원서 목록 조회 API", description = "지원서 목록 조회 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/applications")
public class ApplicationListViewController {

	private final ApplicationViewListService applicationViewListService;

	/**
	 * 지원서 목록 조회
	 *
	 * @param request      지원서 목록 조회 요청 정보
	 * @param generationId 기수 ID - 필수
	 * @param partViewType (ALL, BE, FE, PM, DE) - 필수
	 * @param passViewType (ALL, PASS, FAIL, WAITLISTED) - 필수
	 * @param pageable     페이지네이션 정보
	 * @return 지원서 목록
	 */
	@GetMapping
	public ApiResponse<AdminApplicationsResponse> getApplications(
			@ParameterObject @ModelAttribute ApplicationListRequest request,
			@ParameterObject @PageableDefault(size = 10) Pageable pageable) {

		AdminApplicationsResponse response = applicationViewListService.getApplications(request, pageable);
		return ApiResponse.success(response);
	}
}