package org.cotato.backend.recruit.common.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.admin.error.AdminErrorCode;
import org.cotato.backend.recruit.admin.exception.AdminException;
import org.cotato.backend.recruit.common.email.dto.EmailMessage;
import org.cotato.backend.recruit.domain.email.entity.EmailSendJob;
import org.cotato.backend.recruit.domain.email.repository.EmailSendJobRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender mailSender;
	private final EmailSendJobRepository emailSendJobRepository;

	private static final String HEADER_IMAGE_PATH = "static/email/header.png";
	private static final String BOTTOM_IMAGE_PATH = "static/email/bottom.png";

	/**
	 * 이메일 전송
	 *
	 * @param emailMessage 이메일 메시지
	 * @return 성공 여부
	 */
	public boolean sendEmail(EmailMessage emailMessage) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setTo(emailMessage.getTo());
			helper.setSubject(emailMessage.getSubject());
			helper.setText(emailMessage.getContent(), true);

			// 템플릿 이미지 첨부
			if (emailMessage.isWithTemplateImages()) {
				addTemplateImages(helper);
			}

			mailSender.send(message);
			log.info("이메일 전송 성공: {}", emailMessage.getTo());
			return true;

		} catch (MessagingException e) {
			log.error("이메일 전송 실패: {}", emailMessage.getTo(), e);
			return false;
		}
	}

	/** 템플릿 이미지 첨부 (CID 방식) */
	private void addTemplateImages(MimeMessageHelper helper) throws MessagingException {
		ClassPathResource headerImage = new ClassPathResource(HEADER_IMAGE_PATH);
		ClassPathResource bottomImage = new ClassPathResource(BOTTOM_IMAGE_PATH);

		helper.addInline("header", headerImage, "image/png");
		helper.addInline("bottom", bottomImage, "image/png");
	}

	/**
	 * 여러 이메일을 비동기로 배치 전송
	 *
	 * @param emailMessages 이메일 메시지 리스트
	 * @param jobId 발송 작업 ID
	 */
	@Async("emailTaskExecutor")
	public void sendBatchEmailsAsync(List<EmailMessage> emailMessages, Long jobId) {
		log.info("비동기 이메일 발송 시작 - jobId: {}, 총 {}건", jobId, emailMessages.size());

		int successCount = 0;
		int failCount = 0;

		for (EmailMessage emailMessage : emailMessages) {
			if (sendEmail(emailMessage)) {
				successCount++;
			} else {
				failCount++;
			}
		}

		completeJob(jobId, successCount, failCount);
		log.info("비동기 이메일 발송 완료 - jobId: {}, 성공: {}, 실패: {}", jobId, successCount, failCount);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void completeJob(Long jobId, int successCount, int failCount) {
		EmailSendJob job =
				emailSendJobRepository
						.findById(jobId)
						.orElseThrow(() -> new AdminException(AdminErrorCode.EMAIL_JOB_NOT_FOUND));
		job.complete(successCount, failCount);
		emailSendJobRepository.save(job);
	}
}
