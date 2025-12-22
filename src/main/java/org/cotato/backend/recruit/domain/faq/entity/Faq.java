package org.cotato.backend.recruit.domain.faq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cotato.backend.recruit.domain.faq.enums.FaqType;

@Entity
@Getter
@Table(name = "faqs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Faq {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "faq_id", nullable = false)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "faq_type", nullable = false)
	private FaqType faqType;

	@Column(name = "question", nullable = false)
	private String question;

	@Column(name = "answer", nullable = false)
	private String answer;

	@Column(name = "sequence", nullable = false)
	private Integer sequence;

	@Builder
	public Faq(FaqType faqType, String question, String answer, Integer sequence) {
		this.faqType = faqType;
		this.question = question;
		this.answer = answer;
		this.sequence = sequence;
	}
}
