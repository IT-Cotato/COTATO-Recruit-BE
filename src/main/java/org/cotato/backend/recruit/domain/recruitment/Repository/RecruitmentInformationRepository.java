package org.cotato.backend.recruit.domain.recruitment.Repository;

import org.cotato.backend.recruit.domain.recruitment.entity.RecruitmentInformation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitmentInformationRepository
		extends JpaRepository<RecruitmentInformation, Long> {}
