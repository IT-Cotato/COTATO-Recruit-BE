package org.cotato.backend.recruit.domain.email.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cotato.backend.recruit.domain.email.enums.TemplateType;
import org.cotato.backend.recruit.domain.generation.entity.Generation;

@Entity
@Getter
@Table(name = "email_templates")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailTemplate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "mail_template_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "generation_id", nullable = false)
	private Generation generation;

	@Enumerated(EnumType.STRING)
	@Column(name = "template_type", nullable = false)
	private TemplateType templateType;

	@Column(name = "content", nullable = false)
	private String content;

	@Builder
	public EmailTemplate(Generation generation, TemplateType templateType, String content) {
		this.generation = generation;
		this.templateType = templateType;
		this.content = content;
	}
}
