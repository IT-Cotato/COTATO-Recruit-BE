package org.cotato.backend.recruit.domain.email.repository;

import java.util.Optional;
import org.cotato.backend.recruit.domain.email.entity.RecruitmentEmailTemplate;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruitmentEmailTemplateRepository
		extends JpaRepository<RecruitmentEmailTemplate, Long> {

	/** 기수로 모집 알림 이메일 템플릿 조회 */
	Optional<RecruitmentEmailTemplate> findByGeneration(Generation generation);
}
