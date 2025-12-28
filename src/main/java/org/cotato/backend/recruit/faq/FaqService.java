package org.cotato.backend.recruit.faq;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.faq.entity.Faq;
import org.cotato.backend.recruit.domain.faq.enums.FaqType;
import org.cotato.backend.recruit.domain.faq.repository.FaqRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {

	private final FaqRepository faqRepository;

	public Map<String, List<FaqResponse.FaqItemResponse>> getFaqsGroupedByMap() {

		List<Faq> allFaqs = faqRepository.findAllByOrderBySequenceAsc();

		Map<FaqType, List<FaqResponse.FaqItemResponse>> groupedData =
				allFaqs.stream()
						.collect(
								Collectors.groupingBy(
										Faq::getFaqType,
										Collectors.mapping(
												faq ->
														FaqResponse.FaqItemResponse.builder()
																.id(faq.getId())
																.question(faq.getQuestion())
																.answer(faq.getAnswer())
																.build(),
												Collectors.toList())));

		Map<String, List<FaqResponse.FaqItemResponse>> sortedMap = new LinkedHashMap<>();

		List<FaqType> targetOrder =
				List.of(FaqType.COMMON, FaqType.PM, FaqType.DE, FaqType.FE, FaqType.BE);

		for (FaqType type : targetOrder) {
			if (groupedData.containsKey(type)) {
				sortedMap.put(type.name().toLowerCase(), groupedData.get(type));
			}
		}

		return sortedMap;
	}
}
