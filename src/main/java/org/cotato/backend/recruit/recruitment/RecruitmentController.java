package org.cotato.backend.recruit.recruitment;

import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recruitment")
public class RecruitmentController {

	private final RecruitmentService recruitmentService;

	@GetMapping
	public ApiResponse<RecruitmentResponse> getRecruitment() {
		return ApiResponse.success(recruitmentService.getRecruitmentData());
	}
}
