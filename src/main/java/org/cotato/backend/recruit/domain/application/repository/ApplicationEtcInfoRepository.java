package org.cotato.backend.recruit.domain.application.repository;

import java.util.Optional;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.entity.ApplicationEtcInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationEtcInfoRepository extends JpaRepository<ApplicationEtcInfo, Long> {

	/**
	 * 지원서로 기타 정보 조회
	 *
	 * @param application 지원서
	 * @return 기타 정보 (Optional)
	 */
	Optional<ApplicationEtcInfo> findByApplication(Application application);

	/**
	 * 지원서 ID로 기타 정보 조회
	 *
	 * @param applicationId 지원서 ID
	 * @return 기타 정보 (Optional)
	 */
	Optional<ApplicationEtcInfo> findByApplicationId(Long applicationId);
}
