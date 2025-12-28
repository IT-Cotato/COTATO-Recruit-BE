package org.cotato.backend.recruit.recruitment;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitment.Repository.RecruitmentInformationRepository;
import org.cotato.backend.recruit.domain.recruitment.Repository.RecruitmentNoticeRepository;
import org.cotato.backend.recruit.domain.recruitment.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitment.entity.RecruitmentNotice;
import org.cotato.backend.recruit.domain.recruitment.enums.InformationType;
import org.cotato.backend.recruit.domain.recruitment.enums.NoticeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentService {

	private final RecruitmentNoticeRepository noticeRepository;
	private final RecruitmentInformationRepository informationRepository;

	public RecruitmentResponse getRecruitmentData() {
		// 1. 모든 공지 및 정보 데이터를 가져오기
		List<RecruitmentNotice> notices = noticeRepository.findAll();
		List<RecruitmentInformation> informations = informationRepository.findAll();
		int generationNumber = 0;
		boolean isActive = false;

		// 2. 기수 정보 추출
		if (!notices.isEmpty()) {
			Generation gen = notices.get(0).getGeneration();
			// 엔티티 필드명이 id이므로 getId()를 사용합니다.
			generationNumber = gen.getId().intValue();
			isActive = gen.isRecruitingActive();
		}

		// 3. startDate, endDate 추출
		String startDate =
				informations.stream()
						.filter(i -> i.getInformationType() == InformationType.RECRUITMENT_START)
						.map(RecruitmentInformation::getEventDatetime)
						.findFirst()
						.orElse("");

		String endDate =
				informations.stream()
						.filter(i -> i.getInformationType() == InformationType.RECRUITMENT_END)
						.map(RecruitmentInformation::getEventDatetime)
						.findFirst()
						.orElse("");

		// 4. Schedule 가공 (NoticeType.RECRUITMENT_SCHEDULE)
		List<RecruitmentResponse.ScheduleResponse> schedules =
				notices.stream()
						.filter(n -> n.getNoticeType() == NoticeType.RECRUITMENT_SCHEDULE)
						.map(
								n ->
										RecruitmentResponse.ScheduleResponse.builder()
												.title(n.getScheduleTitle())
												.date(n.getSchedule())
												.build())
						.toList();

		// 4. Parts 가공 (NoticeType.RECRUITMENT_PART)
		List<RecruitmentResponse.PartResponse> parts =
				notices.stream()
						.filter(n -> n.getNoticeType() == NoticeType.RECRUITMENT_PART)
						.map(
								n ->
										RecruitmentResponse.PartResponse.builder()
												.name(n.getPartName())
												.partShort(n.getPartShort())
												.detail(n.getPartDetail())
												.build())
						.toList();

		// 5. Activities 가공 (NoticeType.ACTIVITY_SCHEDULE)
		List<RecruitmentResponse.ActivityResponse> activities =
				notices.stream()
						.filter(n -> n.getNoticeType() == NoticeType.ACTIVITY_SCHEDULE)
						.map(
								n ->
										RecruitmentResponse.ActivityResponse.builder()
												.id(n.getId())
												.name(n.getScheduleTitle())
												.date(n.getSchedule())
												.build())
						.toList();

		// 6. 응답 조립 (기수 번호 등은 첫 번째 데이터나 고정값 활용)
		return RecruitmentResponse.builder()
				.isActive(isActive)
				.generation(generationNumber)
				.startDate(startDate)
				.endDate(endDate)
				.schedule(schedules)
				.parts(parts)
				.activities(activities)
				.build();
	}
}
