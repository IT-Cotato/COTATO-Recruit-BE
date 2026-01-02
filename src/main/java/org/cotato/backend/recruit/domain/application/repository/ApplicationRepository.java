package org.cotato.backend.recruit.domain.application.repository;

import java.util.List;
import java.util.Optional;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.enums.PassStatus;
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
	 * 특정 기수의 특정 합격 상태를 가진 지원서 목록 조회
	 *
	 * @param generation 기수
	 * @param passStatus 합격 상태
	 * @return 지원서 목록
	 */
	List<Application> findByGenerationAndPassStatus(Generation generation, PassStatus passStatus);

	/**
	 * 특정 기수의 특정 합격 상태를 가진 지원서 개수 조회
	 *
	 * @param generation 기수
	 * @param passStatus 합격 상태
	 * @return 지원서 개수
	 */
	long countByGenerationAndPassStatus(Generation generation, PassStatus passStatus);
}
