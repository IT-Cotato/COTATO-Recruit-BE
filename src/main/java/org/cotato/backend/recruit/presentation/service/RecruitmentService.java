package org.cotato.backend.recruit.presentation.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.recruitment.entity.RecruitmentNotice;
import org.cotato.backend.recruit.domain.recruitment.enums.NoticeType;
import org.cotato.backend.recruit.domain.recruitment.repository.RecruitmentNoticeRepository;
import org.cotato.backend.recruit.presentation.dto.response.RecruitmentResponse;
import org.cotato.backend.recruit.presentation.dto.response.RecruitmentScheduleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentService {

	private final RecruitmentNoticeRepository noticeRepository;
	private final RecruitmentInformationService recruitmentInformationService;

	public RecruitmentResponse getRecruitmentData() {

		// 1. 이미 구현된 서비스를 통해 현재 기수와 일정 정보를 가져옴
		RecruitmentScheduleResponse scheduleInfo =
				recruitmentInformationService.getRecruitmentSchedule();

		// 2. 공지 데이터(Part, Activity 등) 가져오기
		List<RecruitmentNotice> notices = noticeRepository.findAll();

		// 3. 기수 번호 및 활성화 여부 추출
		// 이미 구현된 서비스가 activeGeneration을 기준으로 가져오므로 0번째 인덱스 접근보다 안전합니다.
		int generationNumber = scheduleInfo.generationId().intValue();
		boolean isActive = true;

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
				.startDate(String.valueOf(scheduleInfo.applicationStartDate()))
				.endDate(String.valueOf(scheduleInfo.applicationEndDate()))
				.schedule(schedules)
				.parts(parts)
				.activities(activities)
				.build();
	}
}
