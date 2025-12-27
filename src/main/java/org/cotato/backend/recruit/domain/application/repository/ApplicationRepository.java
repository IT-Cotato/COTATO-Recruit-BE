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
}
