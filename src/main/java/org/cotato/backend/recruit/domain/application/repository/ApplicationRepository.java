package org.cotato.backend.recruit.domain.application.repository;

import java.util.Optional;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

	/**
	 * 사용자와 기수로 지원서 조회
	 *
	 * @param user 사용자
	 * @param generation 기수
	 * @return 지원서 (Optional)
	 */
	Optional<Application> findByUserAndGeneration(User user, Generation generation);

	/**
	 * 사용자 ID로 지원서 조회
	 *
	 * @param userId 사용자 ID
	 * @return 지원서 (Optional)
	 */
	Optional<Application> findByUserId(Long userId);

	/**
	 * 사용자와 기수로 제출된 지원서 존재 여부 확인
	 *
	 * @param user 사용자
	 * @param generation 기수
	 * @param isSubmitted 제출 여부
	 * @return 존재 여부
	 */
	boolean existsByUserAndGenerationAndIsSubmitted(
			User user, Generation generation, boolean isSubmitted);
}
