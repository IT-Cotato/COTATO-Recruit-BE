package org.cotato.backend.recruit.admin.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.response.ActivationResponse;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.generation.repository.GenerationRepository;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.cotato.backend.recruit.domain.recruitmentInformation.repository.RecruitmentInformationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentActiveService {

	private final RecruitmentInformationRepository recruitmentInformationRepository;
	private final GenerationRepository generationRepository;

	@Transactional
	public void activateRecruitment(Long generationId, LocalDate startDate, LocalDate endDate) {
		// null 체크
		validate(generationId, startDate, endDate);

		// Generation 생성, id를 그대로 PK로 사용
		Generation generation = Generation.builder().id(generationId).build();

		// 모집 활성화
		generation.updateRecruitmentStatus(true);

		// 지원시작일, 지원종료일 정보 update, 없으면 생성
		updateRecruitmentInformation(
				generation, InformationType.RECRUITMENT_START, startDate.atStartOfDay());
		updateRecruitmentInformation(
				generation, InformationType.RECRUITMENT_END, endDate.atTime(23, 59, 59));
	}

	public ActivationResponse getRecruitmentActivation(Long generationId) {
		Generation generation = generationRepository
				.findById(generationId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기수입니다."));

		LocalDateTime startDate = recruitmentInformationRepository
				.findByGenerationAndInformationType(
						generation, InformationType.RECRUITMENT_START)
				.map(RecruitmentInformation::getEventDatetime)
				.orElse(null);

		LocalDateTime endDate = recruitmentInformationRepository
				.findByGenerationAndInformationType(
						generation, InformationType.RECRUITMENT_END)
				.map(RecruitmentInformation::getEventDatetime)
				.orElse(null);

		return ActivationResponse.builder()
				.generation(generation.getId())
				.startDate(startDate)
				.endDate(endDate)
				.isActive(generation.isRecruitingActive())
				.build();
	}

	private void validate(Long generationId, LocalDate startDate, LocalDate endDate) {
		if (generationId == null || startDate == null || endDate == null) {
			throw new IllegalArgumentException("모집 활성화 정보가 없습니다.");
		}
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
						() -> recruitmentInformationRepository.save(
								RecruitmentInformation.builder()
										.generation(generation)
										.informationType(type)
										.eventDatetime(datetime)
										.build()));
	}
}
