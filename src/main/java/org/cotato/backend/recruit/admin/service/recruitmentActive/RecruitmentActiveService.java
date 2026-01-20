package org.cotato.backend.recruit.admin.service.recruitmentActive;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.service.generation.GenerationAdminService;
import org.cotato.backend.recruit.admin.service.recruitmentInformation.RecruitmentInformationUpserterManager;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용, 쓰기가 필요한 곳만 오버라이드
public class RecruitmentActiveService {

	private final GenerationAdminService generationAdminService;
	private final RecruitmentInformationUpserterManager recruitmentInformationUpserterManager;

	private static final LocalTime END_OF_DAY = LocalTime.of(23, 59, 59);

	@Transactional
	@CacheEvict(
			value = {"activeGeneration", "recruitmentSchedule", "recruitmentStatus"},
			allEntries = true)
	public void activateRecruitment(
			Long generationId,
			boolean isAdditionalRecruitmentActive,
			LocalDate startDate,
			LocalDate endDate) {

		validateDateOrder(startDate, endDate);

		// 1. 기수 조회 혹은 생성
		// 기수 생성할 때 모집 상태도 활성화
		Generation generation = getOrCreateGenerationWithRecruitmentActive(generationId);

		// 2. 모집 상태 업데이트 (활성화 및 추가모집 여부 설정)
		generation.startRecruitment(isAdditionalRecruitmentActive);

		// 3. 모집 기간(RecruitmentInformation) 업데이트 (Upsert)
		recruitmentInformationUpserterManager.upsertDatetime(
				generation, InformationType.RECRUITMENT_START, startDate.atStartOfDay());
		recruitmentInformationUpserterManager.upsertDatetime(
				generation, InformationType.RECRUITMENT_END, endDate.atTime(END_OF_DAY));
	}

	@Transactional
	@CacheEvict(
			value = {"activeGeneration", "recruitmentStatus"},
			allEntries = true)
	public void deactivateRecruitment(Long generationId) {
		Generation generation = generationAdminService.findGeneration(generationId);
		generation.endRecruitment();
	}

	private Generation getOrCreateGenerationWithRecruitmentActive(Long generationId) {
		return generationAdminService
				.findGenerationOptional(generationId)
				.orElseGet(
						() ->
								generationAdminService.saveNewGenerationWithRecruitingActive(
										generationId));
	}

	private void validateDateOrder(LocalDate startDate, LocalDate endDate) {
		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("시작일은 종료일보다 빨라야 합니다.");
		}
	}
}
