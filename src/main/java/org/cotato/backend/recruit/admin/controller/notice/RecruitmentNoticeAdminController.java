package org.cotato.backend.recruit.admin.controller.notice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.notice.RecruitmentNoticeCreateRequest;
import org.cotato.backend.recruit.admin.service.notice.RecruitmentNoticeAdminService;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "모집 공고 관리 API", description = "모집 공고 데이터 등록 관련 API")
@RestController
@RequestMapping("/api/admin/recruitment-notices")
@RequiredArgsConstructor
public class RecruitmentNoticeAdminController {

	private final RecruitmentNoticeAdminService recruitmentNoticeAdminService;

	@Operation(
			summary = "모집 공고 데이터 일괄 등록",
			description =
					"모집 파트 4개, 주요 활동 6개, 모집 일정 5개를 순서에 맞게 한번에 등록합니다. "
							+ "기존 해당 기수의 모집 공고 데이터는 모두 삭제되고 새로 등록됩니다.")
	@PostMapping
	public ApiResponse<Void> createRecruitmentNotices(
			@Valid @RequestBody RecruitmentNoticeCreateRequest request) {
		recruitmentNoticeAdminService.createRecruitmentNotices(request);
		return ApiResponse.success();
	}
}
