package org.cotato.backend.recruit.faq;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/faq")
public class FaqController {

	private final FaqService faqService;

	@GetMapping
	public ApiResponse<Map<String, List<FaqResponse.FaqItemResponse>>> getFaqs() {
		Map<String, List<FaqResponse.FaqItemResponse>> faqs = faqService.getFaqsGroupedByMap();
		return ApiResponse.success(faqs);
	}
}
