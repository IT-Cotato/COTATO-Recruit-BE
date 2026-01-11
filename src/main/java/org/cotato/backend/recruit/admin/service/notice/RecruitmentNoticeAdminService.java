package org.cotato.backend.recruit.admin.service.notice;

import static org.cotato.backend.recruit.admin.error.AdminErrorCode.*;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.admin.dto.request.notice.RecruitmentNoticeCreateRequest;
import org.cotato.backend.recruit.admin.dto.request.notice.RecruitmentNoticeCreateRequest.ActivityRequest;
import org.cotato.backend.recruit.admin.dto.request.notice.RecruitmentNoticeCreateRequest.PartRequest;
import org.cotato.backend.recruit.admin.dto.request.notice.RecruitmentNoticeCreateRequest.ScheduleRequest;
import org.cotato.backend.recruit.admin.exception.AdminException;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.generation.repository.GenerationRepository;
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
	private final GenerationRepository generationRepository;

	/**
	 * 모집 공고 데이터 일괄 등록
	 *
	 * @param request 모집 공고 생성 요청
	 */
	@Transactional
	public void createRecruitmentNotices(RecruitmentNoticeCreateRequest request) {
		// 기수 조회
		Generation generation =
				generationRepository
						.findById(request.generation().longValue())
						.orElseThrow(() -> new AdminException(GENERATION_NOT_FOUND));

		// 기존 데이터 삭제
		recruitmentNoticeRepository.deleteByGeneration(generation);

		List<RecruitmentNotice> notices = new ArrayList<>();

		// 모집 파트 등록 (PM, DE, FE, BE 순서)
		for (PartRequest part : request.parts()) {
			RecruitmentNotice notice =
					RecruitmentNotice.builder()
							.generation(generation)
							.noticeType(NoticeType.RECRUITMENT_PART)
							.partName(part.partType().getPartName())
							.partShort(part.partType().getPartShort())
							.partDetail(part.detail())
							.imageFilename(part.partType().getImageFilename())
							.build();
			notices.add(notice);
		}

		// 주요 활동 일정 등록 (OT, 정기 세션, MT, DevTalk, 코커톤, 데모데이 순서)
		for (ActivityRequest activity : request.activities()) {
			RecruitmentNotice notice =
					RecruitmentNotice.builder()
							.generation(generation)
							.noticeType(NoticeType.ACTIVITY_SCHEDULE)
							.scheduleTitle(activity.activityType().getActivityName())
							.schedule(activity.date())
							.imageFilename(activity.activityType().getImageFilename())
							.build();
			notices.add(notice);
		}

		// 모집 일정 등록 (서류 접수, 서류 합격 발표, 면접 진행, 최종 합격 발표, OT 순서)
		for (ScheduleRequest schedule : request.schedules()) {
			RecruitmentNotice notice =
					RecruitmentNotice.builder()
							.generation(generation)
							.noticeType(NoticeType.RECRUITMENT_SCHEDULE)
							.scheduleTitle(
									schedule.scheduleType().getFullTitle(request.generation()))
							.schedule(schedule.date())
							.build();
			notices.add(notice);
		}

		// 일괄 저장
		recruitmentNoticeRepository.saveAll(notices);
	}
}
