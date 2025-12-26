package org.cotato.backend.recruit.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Provider provider;

	@Column(name = "provider_id", nullable = false)
	private String providerId;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	// 정적 팩토리 메서드 - Google OAuth 회원가입
	public static User createGoogleUser(String email, String name, String providerId) {
		User user = new User();
		user.email = email;
		user.name = name;
		user.role = Role.APPLICANT; // 기본값
		user.provider = Provider.GOOGLE;
		user.providerId = providerId;
		return user;
	}

	// 정적 팩토리 메서드 - 관리자 생성
	public static User createAdmin(
			String email, String name, Provider provider, String providerId) {
		User user = new User();
		user.email = email;
		user.name = name;
		user.role = Role.STAFF;
		user.provider = provider;
		user.providerId = providerId;
		return user;
	}

	public void updateProfile(String name) {
		this.name = name;
	}

	public enum Role {
		APPLICANT,
		STAFF;

		public String getKey() {
			return name();
		}
	}

	public enum Provider {
		GOOGLE
	}
}
