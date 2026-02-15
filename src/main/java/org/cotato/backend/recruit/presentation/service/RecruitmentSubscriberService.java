package org.cotato.backend.recruit.presentation.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.subscriber.entity.RecruitmentSubscriber;
import org.cotato.backend.recruit.domain.subscriber.repository.RecruitmentSubscriberRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecruitmentSubscriberService {

	private final RecruitmentSubscriberRepository recruitmentSubscriberRepository;

	/** 모집 알림 구독 신청 */
	@Transactional
	public void subscribe(String email) {
		Optional<RecruitmentSubscriber> existingSubscriber =
				recruitmentSubscriberRepository.findByEmail(email);

		if (existingSubscriber.isPresent()) {
			// 기존 구독자가 있으면 알림 상태를 초기화하여 재구독 처리
			existingSubscriber.get().resetNotified();
			return;
		}

		try {
			// 신규 구독자 생성 및 저장
			RecruitmentSubscriber subscriber = RecruitmentSubscriber.builder().email(email).build();
			recruitmentSubscriberRepository.save(subscriber);
		} catch (DataIntegrityViolationException e) {
			// 동시성 문제로 인한 중복 삽입 시 기존 구독자 재구독 처리
			recruitmentSubscriberRepository
					.findByEmail(email)
					.ifPresent(RecruitmentSubscriber::resetNotified);
		}
	}
}
