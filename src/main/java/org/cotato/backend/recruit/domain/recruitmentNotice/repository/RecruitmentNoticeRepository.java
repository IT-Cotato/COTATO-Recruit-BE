package org.cotato.backend.recruit.domain.recruitmentNotice.repository;

import java.util.List;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitmentNotice.entity.RecruitmentNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecruitmentNoticeRepository extends JpaRepository<RecruitmentNotice, Long> {
	List<RecruitmentNotice> findAllByGenerationId(Long generationId);

	@Modifying
	@Query("DELETE FROM RecruitmentNotice rn WHERE rn.generation = :generation")
	void deleteByGeneration(@Param("generation") Generation generation);
}
