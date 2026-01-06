package org.cotato.backend.recruit.presentation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.recruitment.repository.RecruitmentNoticeRepository;
import org.cotato.backend.recruit.domain.recruitmentNotice.entity.RecruitmentNotice;
import org.cotato.backend.recruit.domain.recruitmentNotice.enums.NoticeType;
import org.cotato.backend.recruit.presentation.dto.response.RecruitmentResponse;
import org.cotato.backend.recruit.presentation.dto.response.RecruitmentScheduleResponse;
import org.cotato.backend.recruit.presentation.error.ApplicationErrorCode;
import org.cotato.backend.recruit.presentation.exception.ApplicationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentService {

	private final RecruitmentNoticeRepository noticeRepository;
	private final RecruitmentInformationService recruitmentInformationService;

	public RecruitmentResponse getRecruitmentData() {

		// 1. 최신 기수 정보 및 일정 가져오기
		RecruitmentScheduleResponse scheduleInfo =
				recruitmentInformationService.getRecruitmentSchedule();

		// 2. 데이터가 없을 경우 예외 처리
		if (scheduleInfo == null) {
			throw new ApplicationException(ApplicationErrorCode.GENERATION_NOT_FOUND);
		}

		// 3. 활성화 여부 계산
		LocalDateTime now = LocalDateTime.now();
		boolean isActive =
				now.isAfter(scheduleInfo.applicationStartDate())
						&& now.isBefore(scheduleInfo.applicationEndDate());

		// 4. 데이터 조회 및 그룹화
		List<RecruitmentNotice> notices =
				noticeRepository.findAllByGenerationId(scheduleInfo.generationId());
		Map<NoticeType, List<RecruitmentNotice>> grouped =
				notices.stream().collect(Collectors.groupingBy(RecruitmentNotice::getNoticeType));

		// 5. 응답
		return new RecruitmentResponse(
				isActive,
				scheduleInfo.generationId().intValue(),
				String.valueOf(scheduleInfo.applicationStartDate()),
				String.valueOf(scheduleInfo.applicationEndDate()),
				grouped.getOrDefault(NoticeType.RECRUITMENT_SCHEDULE, List.of()).stream()
						.map(RecruitmentResponse.ScheduleResponse::from)
						.toList(),
				grouped.getOrDefault(NoticeType.RECRUITMENT_PART, List.of()).stream()
						.map(RecruitmentResponse.PartResponse::from)
						.toList(),
				grouped.getOrDefault(NoticeType.ACTIVITY_SCHEDULE, List.of()).stream()
						.map(RecruitmentResponse.ActivityResponse::from)
						.toList());
	}
}
