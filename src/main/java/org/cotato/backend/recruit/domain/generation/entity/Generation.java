package org.cotato.backend.recruit.domain.generation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "generations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Generation {

	@Id
	@Column(name = "generation_id")
	private Long id;

	@Column(name = "is_recruiting_active", nullable = false)
	private boolean isRecruitingActive;

	@Column(name = "is_additional_recruitment_active", nullable = false)
	private boolean isAdditionalRecruitmentActive;

	@Builder
	public Generation(Long id, boolean isRecruitingActive, boolean isAdditionalRecruitmentActive) {
		this.id = id;
		this.isRecruitingActive = isRecruitingActive;
		this.isAdditionalRecruitmentActive = isAdditionalRecruitmentActive;
	}

	public void endRecruitment() {
		this.isRecruitingActive = false;
		this.isAdditionalRecruitmentActive = false;
	}

	public void startRecruitment(boolean isAdditionalRecruitmentActive) {
		this.isRecruitingActive = true;
		this.isAdditionalRecruitmentActive = isAdditionalRecruitmentActive;
	}
}
