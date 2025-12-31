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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cotato.backend.recruit.admin.error.AdminErrorCode;
import org.cotato.backend.recruit.admin.exception.AdminException;
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

	@Column(name = "is_sent", nullable = false)
	private boolean isSent = false;

	@Column(name = "sent_at")
	private LocalDateTime sentAt;

	@Builder
	public EmailTemplate(Generation generation, TemplateType templateType, String content) {
		this.generation = generation;
		this.templateType = templateType;
		this.content = content;
		this.isSent = false;
	}

	/** 메일 내용 수정 */
	public void updateContent(String content) {
		this.content = content;
	}

	/** 메일 전송 완료 표시 */
	public void markAsSent() {
		this.isSent = true;
		this.sentAt = LocalDateTime.now();
	}

	/** 이미 전송되었는지 확인 */
	public void validateNotSent() {
		if (this.isSent) {
			throw new AdminException(AdminErrorCode.EMAIL_ALREADY_SENT);
		}
	}
}
