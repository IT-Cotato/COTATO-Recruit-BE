package org.cotato.backend.recruit.admin.service.notice;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.admin.dto.request.notice.RecruitmentNoticeCreateRequest;
import org.cotato.backend.recruit.admin.dto.request.notice.RecruitmentNoticeCreateRequest.ActivityRequest;
import org.cotato.backend.recruit.admin.dto.request.notice.RecruitmentNoticeCreateRequest.PartRequest;
import org.cotato.backend.recruit.admin.dto.request.notice.RecruitmentNoticeCreateRequest.ScheduleRequest;
import org.cotato.backend.recruit.admin.service.generation.GenerationAdminService;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitmentNotice.entity.RecruitmentNotice;
import org.cotato.backend.recruit.domain.recruitmentNotice.enums.NoticeType;
import org.cotato.backend.recruit.domain.recruitmentNotice.repository.RecruitmentNoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentNoticeAdminService {

	private final RecruitmentNoticeRepository recruitmentNoticeRepository;
	private final GenerationAdminService generationAdminService;

	@Transactional
	public void createRecruitmentNotices(RecruitmentNoticeCreateRequest request) {
		Generation generation = generationAdminService.getGenerationById(request.generationId());

		// 기존 데이터 삭제 (Dirty Checking이 아닌 벌크 연산이므로 명시적 호출)
		recruitmentNoticeRepository.deleteByGeneration(generation);

		// 각 카테고리별 공고 엔티티 생성 및 병합
		List<RecruitmentNotice> allNotices = new ArrayList<>();
		allNotices.addAll(createPartNotices(generation, request.parts()));
		allNotices.addAll(createActivityNotices(generation, request.activities()));
		allNotices.addAll(
				createScheduleNotices(generation, request.schedules(), request.generationId()));

		recruitmentNoticeRepository.saveAll(allNotices);
	}

	// 모집 파트 등록 (PM, DE, FE, BE 순서)
	private List<RecruitmentNotice> createPartNotices(
			Generation generation, List<PartRequest> parts) {
		return parts.stream()
				.map(
						part ->
								RecruitmentNotice.builder()
										.generation(generation)
										.noticeType(NoticeType.RECRUITMENT_PART)
										.partName(part.partType().getPartName())
										.partShort(part.partType().getPartShort())
										.partDetail(part.detail())
										.build())
				.toList();
	}

	// 주요 활동 일정 등록 (OT, 정기 세션, MT, DevTalk, 코커톤, 데모데이 순서)
	private List<RecruitmentNotice> createActivityNotices(
			Generation generation, List<ActivityRequest> activities) {
		return activities.stream()
				.map(
						activity ->
								RecruitmentNotice.builder()
										.generation(generation)
										.noticeType(NoticeType.ACTIVITY_SCHEDULE)
										.scheduleTitle(activity.activityType().getActivityName())
										.schedule(activity.date())
										.activityShort(activity.activityType().getActivityShort())
										.build())
				.toList();
	}

	// 모집 절차 일정 등록 (서류 접수, 서류 합격 발표, 면접 진행, 최종 합격 발표, OT 순서)
	private List<RecruitmentNotice> createScheduleNotices(
			Generation generation, List<ScheduleRequest> schedules, Long generationId) {
		return schedules.stream()
				.map(
						schedule ->
								RecruitmentNotice.builder()
										.generation(generation)
										.noticeType(NoticeType.RECRUITMENT_SCHEDULE)
										.scheduleTitle(
												schedule.scheduleType().getFullTitle(generationId))
										.schedule(schedule.date())
										.build())
				.toList();
	}
}
