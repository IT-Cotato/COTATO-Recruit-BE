package org.cotato.backend.recruit.domain.subscriber.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Table(name = "recruitment_subscribers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitmentSubscriber {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "subscriber_id")
	private Long id;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	// 이미 모집 알림이 전달되었는지 여부
	@Column(name = "is_notified", nullable = false)
	private boolean isNotified = false;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Builder
	public RecruitmentSubscriber(String email) {
		this.email = email;
		this.isNotified = false;
	}

	/** 메일 전송 완료 표시 */
	public void markAsNotified() {
		this.isNotified = true;
	}

	/** 알림 상태 초기화 (재구독 시 사용) */
	public void resetNotified() {
		this.isNotified = false;
	}
}
