package org.cotato.backend.recruit.domain.application.entity;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cotato.backend.recruit.domain.application.enums.PassStatus;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.user.entity.User;

@Entity
@Getter
@Table(name = "applications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Application {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "application_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "generation_id", nullable = false)
	private Generation generation;

	@Enumerated(EnumType.STRING)
	@Column(name = "pass_status")
	private PassStatus passStatus;

	@Column(name = "completed_semesters")
	private Integer completedSemesters;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "gender", nullable = false)
	private String gender;

	@Column(name = "birth_date", nullable = false)
	private LocalDate birthDate;

	@Column(name = "phone_number", nullable = false)
	private String phoneNumber;

	@Column(name = "university", nullable = false)
	private String university;

	@Column(name = "major", nullable = false)
	private String major;

	@Column(name = "is_prev_activity", nullable = false)
	private boolean isPrevActivity;

	@Column(name = "is_submitted", nullable = false)
	private boolean isSubmitted;

	@Column(name = "submitted_at", nullable = false)
	private LocalDateTime submittedAt;

	@Column(name = "is_enrolled", nullable = false)
	private boolean isEnrolled;

	@Builder
	public Application(
			User user,
			Generation generation,
			PassStatus passStatus,
			Integer completedSemesters,
			String name,
			String gender,
			LocalDate birthDate,
			String phoneNumber,
			String university,
			String major,
			Boolean isPrevActivity,
			boolean isSubmitted,
			LocalDateTime submittedAt,
			boolean isEnrolled) {
		this.user = user;
		this.generation = generation;
		this.passStatus = passStatus;
		this.completedSemesters = completedSemesters;
		this.name = name;
		this.gender = gender;
		this.birthDate = birthDate;
		this.phoneNumber = phoneNumber;
		this.university = university;
		this.major = major;
		this.isPrevActivity = isPrevActivity;
		this.isSubmitted = isSubmitted;
		this.submittedAt = submittedAt;
		this.isEnrolled = isEnrolled;
	}
}
