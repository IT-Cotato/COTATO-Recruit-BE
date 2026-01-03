package org.cotato.backend.recruit.presentation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.cotato.backend.recruit.domain.recruitmentInformation.repository.RecruitmentInformationRepository;
import org.cotato.backend.recruit.presentation.dto.response.RecruitmentScheduleResponse;
import org.cotato.backend.recruit.presentation.dto.response.RecruitmentStatusResponse;
import org.cotato.backend.recruit.presentation.exception.ApplicationException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentInformationService {

	private final RecruitmentInformationRepository recruitmentInformationRepository;
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
		try {
			generationService.getActiveGeneration();
			return RecruitmentStatusResponse.of(true);
		} catch (ApplicationException e) {
			return RecruitmentStatusResponse.of(false);
		}
	}
}
