package org.cotato.backend.recruit.presentation.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.cotato.backend.recruit.domain.recruitmentInformation.repository.RecruitmentInformationRepository;
import org.cotato.backend.recruit.domain.recruitmentNotice.entity.RecruitmentNotice;
import org.cotato.backend.recruit.domain.recruitmentNotice.enums.NoticeType;
import org.cotato.backend.recruit.domain.recruitmentNotice.repository.RecruitmentNoticeRepository;
import org.cotato.backend.recruit.presentation.dto.response.RecruitmentResponse;
import org.cotato.backend.recruit.presentation.dto.response.RecruitmentScheduleResponse;
import org.cotato.backend.recruit.presentation.dto.response.RecruitmentStatusResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentService {

	private final RecruitmentInformationRepository recruitmentInformationRepository;
	private final RecruitmentNoticeRepository noticeRepository;
	private final GenerationService generationService;

	/**
	 * 현재 모집 중인 기수의 일정 조회 (캐시 적용)
	 *
	 * @return 모집 일정 응답
	 */
	@Cacheable(value = "recruitmentSchedule", key = "'current'")
	public RecruitmentScheduleResponse getRecruitmentSchedule() {
		Generation activeGeneration = generationService.getActiveGeneration();

		List<RecruitmentInformation> informations =
				recruitmentInformationRepository.findByGeneration(activeGeneration);

		// InformationType별로 그룹화
		Map<InformationType, LocalDateTime> scheduleMap =
				informations.stream()
						.collect(
								Collectors.toMap(
										RecruitmentInformation::getInformationType,
										RecruitmentInformation::getEventDatetime));

		return RecruitmentScheduleResponse.of(activeGeneration.getId(), scheduleMap);
	}

	/**
	 * 현재 모집 활성화 여부 조회
	 *
	 * @return 모집 활성화 여부 응답
	 */
	public RecruitmentStatusResponse checkRecruitmentStatus() {
		Long generationId = generationService.getActiveGenerationId();
		boolean isActive = generationId != null;
		return RecruitmentStatusResponse.of(isActive, generationId);
	}

	/**
	 * 모집 공고 데이터 조회 (최신 기수 정보 및 일정)
	 *
	 * @return 모집 공고 응답
	 */
	public RecruitmentResponse getRecruitmentData() {

		// 1. 최신 기수 정보 및 일정 가져오기 (활성화 여부와 관계없이)
		Generation latestGeneration = generationService.getLatestGeneration();

		List<RecruitmentInformation> informations =
				recruitmentInformationRepository.findByGeneration(latestGeneration);

		// InformationType별로 그룹화
		Map<InformationType, LocalDateTime> scheduleMap =
				informations.stream()
						.collect(
								Collectors.toMap(
										RecruitmentInformation::getInformationType,
										RecruitmentInformation::getEventDatetime));

		// 2. 데이터 조회 및 그룹화
		List<RecruitmentNotice> notices =
				noticeRepository.findAllByGenerationId(latestGeneration.getId());
		Map<NoticeType, List<RecruitmentNotice>> grouped =
				notices.stream()
						.sorted(Comparator.comparing(RecruitmentNotice::getId))
						.collect(Collectors.groupingBy(RecruitmentNotice::getNoticeType));

		// 3. 응답
		return new RecruitmentResponse(
				latestGeneration.getId().intValue(),
				Optional.ofNullable(scheduleMap.get(InformationType.RECRUITMENT_START))
						.map(LocalDateTime::toString)
						.orElse(null),
				Optional.ofNullable(scheduleMap.get(InformationType.RECRUITMENT_END))
						.map(LocalDateTime::toString)
						.orElse(null),
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
