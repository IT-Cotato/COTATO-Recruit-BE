package org.cotato.backend.recruit.domain.recruitment.entity;

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
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitment.enums.NoticeType;

@Entity
@Getter
@Table(name = "recruitment_notices")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitmentNotice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notice_id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "generation_id", nullable = false)
	private Generation generation;

	@Enumerated(EnumType.STRING)
	@Column(name = "notice_type", nullable = false)
	private NoticeType noticeType;

	@Column(name = "schedule_title")
	private String scheduleTitle;

	@Column(name = "schedule_desc")
	private String scheduleDesc;

	@Column(name = "part_name")
	private String partName;

	@Column(name = "part_short_desc")
	private String partShortDesc;

	@Column(name = "part_detail_desc")
	private String partDetailDesc;

	@Builder
	public RecruitmentNotice(
			Generation generation,
			NoticeType noticeType,
			String scheduleTitle,
			String scheduleDesc,
			String partName,
			String partShortDesc,
			String partDetailDesc) {
		this.generation = generation;
		this.noticeType = noticeType;
		this.scheduleTitle = scheduleTitle;
		this.scheduleDesc = scheduleDesc;
		this.partName = partName;
		this.partShortDesc = partShortDesc;
		this.partDetailDesc = partDetailDesc;
	}
}
