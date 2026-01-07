package org.cotato.backend.recruit.presentation.service;

import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.subscriber.entity.RecruitmentSubscriber;
import org.cotato.backend.recruit.domain.subscriber.repository.RecruitmentSubscriberRepository;
import org.cotato.backend.recruit.presentation.error.PresentationErrorCode;
import org.cotato.backend.recruit.presentation.exception.PresentationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecruitmentSubscriberService {

	private final RecruitmentSubscriberRepository recruitmentSubscriberRepository;

	/** 모집 알림 구독 신청 */
	@Transactional
	public void subscribe(String email) {
		// 이메일 중복 체크
		if (recruitmentSubscriberRepository.existsByEmail(email)) {
			throw new PresentationException(PresentationErrorCode.ALREADY_SUBSCRIBED);
		}

		// 구독자 생성 및 저장
		RecruitmentSubscriber subscriber = RecruitmentSubscriber.builder().email(email).build();

		recruitmentSubscriberRepository.save(subscriber);
	}
}
