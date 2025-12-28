package org.cotato.backend.recruit.domain.recruitment.repository;

import java.util.List;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitment.entity.RecruitmentInformation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitmentInformationRepository
		extends JpaRepository<RecruitmentInformation, Long> {

	List<RecruitmentInformation> findByGeneration(Generation generation);
}
