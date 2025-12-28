package org.cotato.backend.recruit.domain.generation.repository;

import java.util.Optional;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenerationRepository extends JpaRepository<Generation, Long> {

	/**
	 * 현재 모집 중인 기수 조회
	 *
	 * @param isRecruitingActive 모집 활성화 여부
	 * @return 모집 중인 기수 (Optional)
	 */
	Optional<Generation> findByIsRecruitingActive(boolean isRecruitingActive);
}
public interface GenerationRepository extends JpaRepository<Generation, Long> {}
