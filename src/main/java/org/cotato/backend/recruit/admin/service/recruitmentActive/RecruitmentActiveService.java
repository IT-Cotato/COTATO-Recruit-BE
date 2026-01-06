package org.cotato.backend.recruit.admin.service.recruitmentActive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.service.generation.GenerationAdminService;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.cotato.backend.recruit.domain.recruitmentInformation.repository.RecruitmentInformationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecruitmentActiveService {

	private final RecruitmentInformationRepository recruitmentInformationRepository;
	private final GenerationAdminService generationAdminService;

	@Transactional
	public void activateRecruitment(Long generationId, LocalDate startDate, LocalDate endDate) {
		// startDate < endDate
		validate(generationId, startDate, endDate);

		// 기존 Generation인지 확인
		Optional<Generation> generation =
				generationAdminService.findGenerationOptional(generationId);

		// 이미 생성됐으면 모집기간 활성화 및 지원시작일, 종료일 업데이트
		if (generation.isPresent()) {
			generation.get().updateRecruitmentStatus(true);
			updateRecruitmentInformation(
					generation.get(), InformationType.RECRUITMENT_START, startDate.atStartOfDay());
			updateRecruitmentInformation(
					generation.get(), InformationType.RECRUITMENT_END, endDate.atTime(23, 59, 59));
			return;
		}

		// 기수가 생성되지 않았으면 생성
		Generation newGeneration = generationAdminService.saveGeneration(generationId);

		// 모집 활성화
		newGeneration.updateRecruitmentStatus(true);

		// 지원시작일, 지원종료일 정보 update, 없으면 생성
		updateRecruitmentInformation(
				newGeneration, InformationType.RECRUITMENT_START, startDate.atStartOfDay());
		updateRecruitmentInformation(
				newGeneration, InformationType.RECRUITMENT_END, endDate.atTime(23, 59, 59));
	}

	private void validate(Long generationId, LocalDate startDate, LocalDate endDate) {
		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("시작일은 종료일보다 빨라야 합니다.");
		}
	}

	private void updateRecruitmentInformation(
			Generation generation, InformationType type, LocalDateTime datetime) {
		recruitmentInformationRepository
				.findByGenerationAndInformationType(generation, type)
				.ifPresentOrElse(
						info -> info.updateEventDatetime(datetime),
						() ->
								recruitmentInformationRepository.save(
										RecruitmentInformation.builder()
												.generation(generation)
												.informationType(type)
												.eventDatetime(datetime)
												.build()));
	}
}
