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
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cotato.backend.recruit.domain.application.enums.ApplicationPartType;
import org.cotato.backend.recruit.domain.application.enums.PassStatus;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.user.entity.User;
import org.cotato.backend.recruit.presentation.error.PresentationErrorCode;
import org.cotato.backend.recruit.presentation.exception.PresentationException;

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

	@Enumerated(EnumType.STRING)
	@Column(name = "application_part_type")
	private ApplicationPartType applicationPartType;

	@Column(name = "completed_semesters")
	private Integer completedSemesters;

	@Column(name = "name")
	private String name;

	@Column(name = "gender")
	private String gender;

	@Column(name = "birth_date")
	private LocalDate birthDate;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "university")
	private String university;

	@Column(name = "major")
	private String major;

	@Column(name = "is_prev_activity")
	private Boolean isPrevActivity;

	@Column(name = "is_submitted", nullable = false)
	private Boolean isSubmitted;

	@Column(name = "submitted_at")
	private LocalDateTime submittedAt;

	@Column(name = "is_enrolled")
	private Boolean isEnrolled;

	@Column(name = "pdf_file_key")
	private String pdfFileKey;

	@Column(name = "pdf_file_url")
	private String pdfFileUrl;

	// 정적 팩토리 메서드 - 새 지원서 생성
	public static Application createNew(User user, Generation generation) {
		Application application = new Application();
		application.user = user;
		application.generation = generation;
		application.isSubmitted = false;
		return application;
	}

	// 권한 검증
	public void validateUser(Long userId) {
		if (!this.user.getId().equals(userId)) {
			throw new PresentationException(PresentationErrorCode.APPLICATION_FORBIDDEN);
		}
	}

	// 기본 인적사항 업데이트
	public void updateBasicInfo(
			String name,
			String gender,
			LocalDate birthDate,
			String phoneNumber,
			String university,
			String major,
			Integer completedSemesters,
			Boolean isPrevActivity,
			Boolean isEnrolled,
			ApplicationPartType applicationPartType) {
		// 이미 제출된 지원서인지 확인
		if (this.isSubmitted) {
			throw new PresentationException(PresentationErrorCode.ALREADY_SUBMITTED);
		}

		this.name = name;
		this.gender = gender;
		this.birthDate = birthDate;
		this.phoneNumber = phoneNumber;
		this.university = university;
		this.major = major;
		this.completedSemesters = completedSemesters;
		this.isPrevActivity = isPrevActivity;
		this.isEnrolled = isEnrolled;
		this.applicationPartType = applicationPartType;
	}

	// PDF 정보 업데이트
	public void updatePdfInfo(String pdfFileUrl, String pdfFileKey) {
		// 이미 제출된 지원서인지 확인
		if (this.isSubmitted) {
			throw new PresentationException(PresentationErrorCode.ALREADY_SUBMITTED);
		}
		this.pdfFileUrl = pdfFileUrl;
		this.pdfFileKey = pdfFileKey;
	}

	public void updatePassStatus(PassStatus passStatus) {
		this.passStatus = passStatus;
	}

	// 제출 처리
	public void submit() {
		// 이미 제출된 지원서인지 확인
		if (this.isSubmitted) {
			throw new PresentationException(PresentationErrorCode.ALREADY_SUBMITTED);
		}

		// 필수 항목 검증
		validateRequiredFields();

		this.passStatus = PassStatus.PENDING;
		this.isSubmitted = true;
		this.submittedAt = LocalDateTime.now();
	}

	// 제출 전 필수 항목 검증
	private void validateRequiredFields() {
		validateBasicInfo();
	}

	private void validateBasicInfo() {
		validateNotBlank(this.name);
		validateNotBlank(this.gender);
		validateNotBlank(this.phoneNumber);
		validateNotBlank(this.university);
		validateNotBlank(this.major);
		validateNotNull(this.birthDate);
		validateNotNull(this.completedSemesters);
		validateNotNull(this.isPrevActivity);
		validateNotNull(this.applicationPartType);
	}

	private void validateNotBlank(String value) {
		if (value == null || value.isBlank()) {
			throw new PresentationException(PresentationErrorCode.REQUIRED_FIELD_MISSING);
		}
	}

	private void validateNotNull(Object value) {
		if (value == null) {
			throw new PresentationException(PresentationErrorCode.REQUIRED_FIELD_MISSING);
		}
	}
}
