package org.cotato.backend.recruit.domain.recruitmentInformation.entity;

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
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;

@Entity
@Getter
@Table(name = "recruitment_informations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitmentInformation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "information_id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "generation_id", nullable = false)
	private Generation generation;

	@Enumerated(EnumType.STRING)
	@Column(name = "information_type", nullable = false)
	private InformationType informationType;

	@Column(name = "event_datetime", nullable = false)
	private LocalDateTime eventDatetime;

	@Builder
	public RecruitmentInformation(
			Generation generation, InformationType informationType, LocalDateTime eventDatetime) {
		this.generation = generation;
		this.informationType = informationType;
		this.eventDatetime = eventDatetime;
	}

	public void updateEventDatetime(LocalDateTime eventDatetime) {
		this.eventDatetime = eventDatetime;
	}
}
