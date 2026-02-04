package org.cotato.backend.recruit.domain.generation.repository;

import java.util.List;
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

	/**
	 * 모집 활성화 상태인 모든 기수 조회
	 *
	 * @param isRecruitingActive 모집 활성화 여부
	 * @return 모집 중인 기수 목록
	 */
	List<Generation> findAllByIsRecruitingActive(boolean isRecruitingActive);

	/**
	 * 최신 기수 조회 (id 내림차순 정렬)
	 *
	 * @return 최신 기수 (Optional)
	 */
	Optional<Generation> findFirstByOrderByIdDesc();
}
