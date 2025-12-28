package org.cotato.backend.recruit.domain.recruitmentInformation.repository;

import java.util.Optional;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitmentInformationRepository
		extends JpaRepository<RecruitmentInformation, Long> {
	Optional<RecruitmentInformation> findByGenerationAndInformationType(
			Generation generation, InformationType informationType);
}
