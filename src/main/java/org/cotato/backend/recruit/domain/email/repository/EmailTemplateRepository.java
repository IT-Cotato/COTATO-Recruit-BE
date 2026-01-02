package org.cotato.backend.recruit.domain.email.repository;

import java.util.Optional;
import org.cotato.backend.recruit.domain.email.entity.EmailTemplate;
import org.cotato.backend.recruit.domain.email.enums.TemplateType;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

	/**
	 * 기수와 템플릿 타입으로 이메일 템플릿 조회
	 *
	 * @param generation 기수
	 * @param templateType 템플릿 타입
	 * @return 이메일 템플릿 (Optional)
	 */
	Optional<EmailTemplate> findByGenerationAndTemplateType(
			Generation generation, TemplateType templateType);
}
