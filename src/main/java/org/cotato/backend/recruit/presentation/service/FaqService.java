package org.cotato.backend.recruit.presentation.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.faq.enums.FaqType;
import org.cotato.backend.recruit.domain.faq.repository.FaqRepository;
import org.cotato.backend.recruit.presentation.dto.response.FaqResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {

	private final FaqRepository faqRepository;

	public List<FaqResponse> getFaqsByType(FaqType type) {
		return faqRepository.findAllByFaqTypeOrderBySequenceAsc(type).stream()
				.map(FaqResponse::from)
				.toList();
	}
}
