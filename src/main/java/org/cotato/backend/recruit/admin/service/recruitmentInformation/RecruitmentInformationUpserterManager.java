package org.cotato.backend.recruit.admin.service.recruitmentInformation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.error.AdminErrorCode;
import org.cotato.backend.recruit.admin.exception.AdminException;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.cotato.backend.recruit.domain.recruitmentInformation.repository.RecruitmentInformationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentInformationUpserterManager {

	private final RecruitmentInformationRepository recruitmentInformationRepository;

	public List<RecruitmentInformation> findAllByGeneration(Generation generation) {
		return recruitmentInformationRepository.findByGeneration(generation);
	}

	public RecruitmentInformation getOrThrow(Generation generation, InformationType type) {
		return recruitmentInformationRepository
				.findByGenerationAndInformationType(generation, type)
				.orElseThrow(
						() -> new AdminException(AdminErrorCode.RECRUITMENT_INFORMATION_NOT_FOUND));
	}

	@Transactional
	public void upsertDatetime(
			Generation generation, InformationType type, LocalDateTime datetime) {
		if (datetime == null) {
			return;
		}

		RecruitmentInformation info =
				recruitmentInformationRepository
						.findByGenerationAndInformationType(generation, type)
						.orElseGet(
								() ->
										RecruitmentInformation.builder()
												.generation(generation)
												.informationType(type)
												.build());

		info.updateEventDatetime(datetime);

		// 신규 엔티티면 insert 필요
		if (info.getId() == null) {
			recruitmentInformationRepository.save(info);
		}
	}

	@Transactional
	public void upsertDateStartOfDay(Generation generation, InformationType type, LocalDate date) {
		if (date == null) {
			return;
		}
		upsertDatetime(generation, type, date.atStartOfDay());
	}
}
