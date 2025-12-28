package org.cotato.backend.recruit.faq;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FaqResponse {
	@Getter
	@Builder
	@JsonPropertyOrder({"id", "question", "answer"})
	public static class FaqItemResponse {
		private Long id;
		private String question;
		private String answer;
	}
}
