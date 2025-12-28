package org.cotato.backend.recruit.domain.recruitment.repository;

import java.util.List;
import java.util.Optional;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitment.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitment.enums.InformationType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitmentInformationRepository
		extends JpaRepository<RecruitmentInformation, Long> {

	Optional<RecruitmentInformation> findByGenerationAndInformationType(
			Generation generation, InformationType informationType);

	List<RecruitmentInformation> findByGeneration(Generation generation);
}
