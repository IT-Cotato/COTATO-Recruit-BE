package org.cotato.backend.recruit.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.cotato.backend.recruit.domain.faq.enums.FaqType;
import org.cotato.backend.recruit.presentation.dto.response.FaqResponse;
import org.cotato.backend.recruit.presentation.service.FaqService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "FAQ API", description = "FAQ 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/faq")
public class FaqController {

	private final FaqService faqService;

	@Operation(summary = "FAQ 조회", description = "타입별 FAQ 질문을 조회합니다. (인증 불필요)")
	@GetMapping
	public ApiResponse<List<FaqResponse>> getFaqs(
			@RequestParam(name = "type") FaqType type // 쿼리 파라미터로 타입을 받음
			) {
		List<FaqResponse> faqs = faqService.getFaqsByType(type);
		return ApiResponse.success(faqs);
	}
}
