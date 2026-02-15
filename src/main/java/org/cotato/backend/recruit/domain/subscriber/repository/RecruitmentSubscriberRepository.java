package org.cotato.backend.recruit.domain.subscriber.repository;

import java.util.List;
import java.util.Optional;
import org.cotato.backend.recruit.domain.subscriber.entity.RecruitmentSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruitmentSubscriberRepository
		extends JpaRepository<RecruitmentSubscriber, Long> {

	/** 아직 알림을 받지 않은 구독자 수 조회 */
	long countByIsNotified(boolean isNotified);

	/** 아직 알림을 받지 않은 모든 구독자 조회 */
	List<RecruitmentSubscriber> findAllByIsNotified(boolean isNotified);

	/** 이메일로 구독자 조회 */
	Optional<RecruitmentSubscriber> findByEmail(String email);
}
