package org.cotato.backend.recruit.admin.service.email;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.response.email.RecruitmentEmailSendResponse;
import org.cotato.backend.recruit.admin.error.AdminErrorCode;
import org.cotato.backend.recruit.admin.exception.AdminException;
import org.cotato.backend.recruit.admin.service.generation.GenerationAdminService;
import org.cotato.backend.recruit.common.email.dto.EmailMessage;
import org.cotato.backend.recruit.common.email.service.EmailService;
import org.cotato.backend.recruit.domain.email.entity.RecruitmentEmailTemplate;
import org.cotato.backend.recruit.domain.email.repository.RecruitmentEmailTemplateRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.subscriber.entity.RecruitmentSubscriber;
import org.cotato.backend.recruit.domain.subscriber.repository.RecruitmentSubscriberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentNotificationEmailSendService {

	private final RecruitmentEmailTemplateRepository recruitmentEmailTemplateRepository;
	private final RecruitmentSubscriberRepository recruitmentSubscriberRepository;
	private final GenerationAdminService generationAdminService;
	private final RecruitmentEmailTemplateService recruitmentEmailTemplateService;
	private final EmailService emailService;

	/**
	 * 모집 알림 이메일 전송
	 *
	 * @param generationId 기수 ID (null이면 현재 활성화된 기수 사용)
	 * @return 이메일 전송 응답
	 */
	@Transactional
	public RecruitmentEmailSendResponse sendRecruitmentNotificationEmails(Long generationId) {
		Generation generation = generationAdminService.getGenerationOrActive(generationId);

		// 모집 알림 메일은 모집이 활성화되어 있어야만 전송 가능
		validateRecruitmentActive(generation);

		RecruitmentEmailTemplate template = validateAndGetTemplate(generation);

		// 아직 알림을 받지 않은 구독자 조회
		List<RecruitmentSubscriber> subscribers =
				recruitmentSubscriberRepository.findAllByIsNotified(false);
		if (subscribers.isEmpty()) {
			return RecruitmentEmailSendResponse.of(0L, 0L, null, generation.getId());
		}

		List<EmailMessage> emailMessages =
				createEmailMessages(subscribers, template, generation.getId());

		long successCount = emailService.sendBatchEmails(emailMessages);
		long failCount = emailMessages.size() - successCount;

		markTemplateAsSent(template);
		markSubscribersAsNotified(subscribers);

		return RecruitmentEmailSendResponse.of(
				successCount, failCount, template.getSentAt(), generation.getId());
	}

	/** 템플릿 조회 및 검증 */
	private RecruitmentEmailTemplate validateAndGetTemplate(Generation generation) {
		RecruitmentEmailTemplate template =
				recruitmentEmailTemplateService.getOrCreateTemplate(generation);
		template.validateNotSent();
		return template;
	}

	/** 모집 활성화 상태 검증 */
	private void validateRecruitmentActive(Generation generation) {
		if (!generation.isRecruitingActive()) {
			throw new AdminException(AdminErrorCode.RECRUITMENT_NOT_ACTIVE);
		}
	}

	/** 이메일 메시지 생성 */
	private List<EmailMessage> createEmailMessages(
			List<RecruitmentSubscriber> subscribers,
			RecruitmentEmailTemplate template,
			Long generationId) {
		String subject = generateEmailSubject(generationId);
		String htmlContent = template.getContent();

		return subscribers.stream()
				.map(
						subscriber ->
								EmailMessage.builder()
										.to(subscriber.getEmail())
										.subject(subject)
										.content(htmlContent)
										.build())
				.collect(Collectors.toList());
	}

	/** 이메일 제목 생성 */
	private String generateEmailSubject(Long generationId) {
		return String.format("[COTATO %d기] 모집 시작 안내", generationId);
	}

	/** 전송 완료 표시 */
	private void markTemplateAsSent(RecruitmentEmailTemplate template) {
		template.markAsSent();
		recruitmentEmailTemplateRepository.save(template);
	}

	/** 구독자들의 알림 완료 표시 */
	private void markSubscribersAsNotified(List<RecruitmentSubscriber> subscribers) {
		subscribers.forEach(RecruitmentSubscriber::markAsNotified);
		recruitmentSubscriberRepository.saveAll(subscribers);
	}
}
