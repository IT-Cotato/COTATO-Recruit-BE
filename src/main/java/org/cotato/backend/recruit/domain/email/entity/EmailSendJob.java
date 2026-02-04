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
import org.cotato.backend.recruit.domain.email.enums.EmailJobType;
import org.cotato.backend.recruit.domain.generation.entity.Generation;

@Entity
@Getter
@Table(name = "email_send_jobs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailSendJob {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "email_send_job_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "generation_id", nullable = false)
	private Generation generation;

	@Enumerated(EnumType.STRING)
	@Column(name = "job_type", nullable = false)
	private EmailJobType jobType;

	@Column(name = "total_count", nullable = false)
	private int totalCount;

	@Column(name = "success_count", nullable = false)
	private int successCount;

	@Column(name = "fail_count", nullable = false)
	private int failCount;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	@Builder
	public EmailSendJob(Generation generation, EmailJobType jobType, int totalCount) {
		this.generation = generation;
		this.jobType = jobType;
		this.totalCount = totalCount;
		this.successCount = 0;
		this.failCount = 0;
		this.createdAt = LocalDateTime.now();
	}

	public void complete(int successCount, int failCount) {
		this.successCount = successCount;
		this.failCount = failCount;
		this.completedAt = LocalDateTime.now();
	}

	public boolean isCompleted() {
		return completedAt != null;
	}
}
