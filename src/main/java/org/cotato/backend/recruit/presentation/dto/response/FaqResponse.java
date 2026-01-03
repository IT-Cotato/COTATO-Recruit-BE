package org.cotato.backend.recruit.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.cotato.backend.recruit.domain.faq.entity.Faq;

@JsonPropertyOrder({"id", "question", "answer"})
public record FaqResponse(Long id, String question, String answer) {
	public static FaqResponse from(Faq faq) {
		return new FaqResponse(faq.getId(), faq.getQuestion(), faq.getAnswer());
	}
}
