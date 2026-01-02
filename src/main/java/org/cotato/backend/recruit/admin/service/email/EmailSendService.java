package org.cotato.backend.recruit.admin.service.email;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.admin.dto.response.email.EmailSendResponse;
import org.cotato.backend.recruit.admin.service.application.ApplicationAdminService;
import org.cotato.backend.recruit.admin.service.generation.GenerationAdminService;
import org.cotato.backend.recruit.common.email.dto.EmailMessage;
import org.cotato.backend.recruit.common.email.service.EmailService;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.enums.PassStatus;
import org.cotato.backend.recruit.domain.email.entity.EmailTemplate;
import org.cotato.backend.recruit.domain.email.enums.TemplateType;
import org.cotato.backend.recruit.domain.email.repository.EmailTemplateRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailSendService {

	private final EmailTemplateRepository emailTemplateRepository;
	private final ApplicationAdminService applicationAdminService;
	private final GenerationAdminService generationAdminService;
	private final EmailTemplateService emailTemplateService;
	private final EmailService emailService;

	/**
	 * 이메일 전송
	 *
	 * @param templateType 템플릿 타입
	 * @param generationId 기수 ID (null이면 현재 활성화된 기수 사용)
	 * @return 이메일 전송 응답
	 */
	@Transactional
	public EmailSendResponse sendEmails(TemplateType templateType, Long generationId) {
		Generation generation = generationAdminService.getGenerationOrActive(generationId);
		PassStatus passStatus = templateType.toPassStatus();
		EmailTemplate emailTemplate = validateAndGetTemplate(generation, templateType);

		List<Application> applications = getRecipients(generation, passStatus);
		if (applications.isEmpty()) {
			return EmailSendResponse.of(templateType, 0L, 0L, null, generation.getId());
		}

		List<EmailMessage> emailMessages =
				createEmailMessages(applications, emailTemplate, templateType, generation.getId());

		long successCount = emailService.sendBatchEmails(emailMessages);
		long failCount = emailMessages.size() - successCount;

		markTemplateAsSent(emailTemplate);

		return EmailSendResponse.of(
				templateType,
				successCount,
				failCount,
				emailTemplate.getSentAt(),
				generation.getId());
	}

	/** 템플릿 조회 및 검증 */
	private EmailTemplate validateAndGetTemplate(Generation generation, TemplateType templateType) {
		EmailTemplate emailTemplate =
				emailTemplateService.getOrCreateEmailTemplate(generation, templateType);
		emailTemplate.validateNotSent();
		return emailTemplate;
	}

	/** 대상자 조회 */
	private List<Application> getRecipients(Generation generation, PassStatus passStatus) {
		return applicationAdminService.findByGenerationAndPassStatus(generation, passStatus);
	}

	/** 이메일 메시지 생성 */
	private List<EmailMessage> createEmailMessages(
			List<Application> applications,
			EmailTemplate emailTemplate,
			TemplateType templateType,
			Long generationId) {
		String subject = generateEmailSubject(templateType, generationId);
		String htmlTemplate = emailTemplate.getContent();

		return applications.stream()
				.map(
						application ->
								EmailMessage.builder()
										.to(application.getUser().getEmail())
										.subject(subject)
										.content(htmlTemplate)
										.build())
				.collect(Collectors.toList());
	}

	/** 이메일 제목 생성 */
	private String generateEmailSubject(TemplateType templateType, Long generationId) {
		String statusText = templateType.getDescription();
		return String.format("[COTATO %d기] %s 안내", generationId, statusText);
	}

	/** 전송 완료 표시 */
	private void markTemplateAsSent(EmailTemplate emailTemplate) {
		emailTemplate.markAsSent();
		emailTemplateRepository.save(emailTemplate);
	}
}
