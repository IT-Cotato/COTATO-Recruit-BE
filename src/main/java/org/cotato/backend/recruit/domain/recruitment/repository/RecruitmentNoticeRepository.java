package org.cotato.backend.recruit.domain.recruitment.repository;

import java.util.List;
import org.cotato.backend.recruit.domain.recruitmentNotice.entity.RecruitmentNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitmentNoticeRepository extends JpaRepository<RecruitmentNotice, Long> {
	List<RecruitmentNotice> findAllByGenerationId(Long generationId);
}
