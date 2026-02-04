package org.cotato.backend.recruit.common.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.common.email.dto.EmailMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

	private final JavaMailSender mailSender;
	private final EmailSendJobService emailSendJobService;
	private final Executor emailSendExecutor;

	private static final String HEADER_IMAGE_PATH = "static/email/header.png";
	private static final String BOTTOM_IMAGE_PATH = "static/email/bottom.png";
	private static final int MAX_RETRY_COUNT = 3;
	private static final long RETRY_DELAY_MS = 1000;
	private static final int BATCH_SIZE = 20;
	private static final long BATCH_DELAY_MS = 500;

	public EmailService(
			JavaMailSender mailSender,
			EmailSendJobService emailSendJobService,
			@Qualifier("emailSendExecutor") Executor emailSendExecutor) {
		this.mailSender = mailSender;
		this.emailSendJobService = emailSendJobService;
		this.emailSendExecutor = emailSendExecutor;
	}

	/**
	 * 이메일 전송 (재시도 로직 포함)
	 *
	 * @param emailMessage 이메일 메시지
	 * @return 성공 여부
	 */
	public boolean sendEmail(EmailMessage emailMessage) {
		return sendEmailWithRetry(emailMessage, 0);
	}

	/** 재시도 로직이 포함된 이메일 전송 */
	private boolean sendEmailWithRetry(EmailMessage emailMessage, int retryCount) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setTo(emailMessage.getTo());
			helper.setSubject(emailMessage.getSubject());
			helper.setText(emailMessage.getContent(), true);

			if (emailMessage.isWithTemplateImages()) {
				addTemplateImages(helper);
			}

			mailSender.send(message);
			log.info("이메일 전송 성공: {}", emailMessage.getTo());
			return true;

		} catch (MessagingException | MailException e) {
			if (retryCount < MAX_RETRY_COUNT) {
				log.warn(
						"이메일 전송 실패, 재시도 {}/{}: {}",
						retryCount + 1,
						MAX_RETRY_COUNT,
						emailMessage.getTo());
				try {
					Thread.sleep(RETRY_DELAY_MS * (retryCount + 1));
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					return false;
				}
				return sendEmailWithRetry(emailMessage, retryCount + 1);
			}
			log.error("이메일 전송 최종 실패 ({}회 재시도 후): {}", MAX_RETRY_COUNT, emailMessage.getTo(), e);
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
	 * 여러 이메일을 비동기로 병렬 배치 전송 - 배치 단위로 나눠서 병렬 전송 - 각 배치 사이에 딜레이를 두어 SMTP 서버 부하 분산
	 *
	 * @param emailMessages 이메일 메시지 리스트
	 * @param jobId 발송 작업 ID
	 */
	@Async("emailTaskExecutor")
	public void sendBatchEmailsAsync(List<EmailMessage> emailMessages, Long jobId) {
		log.info("비동기 이메일 병렬 발송 시작 - jobId: {}, 총 {}건", jobId, emailMessages.size());

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failCount = new AtomicInteger(0);

		List<List<EmailMessage>> batches = partitionList(emailMessages, BATCH_SIZE);

		for (int i = 0; i < batches.size(); i++) {
			List<EmailMessage> batch = batches.get(i);
			log.info("배치 {}/{} 처리 중 - {}건", i + 1, batches.size(), batch.size());

			List<CompletableFuture<Boolean>> futures =
					batch.stream()
							.map(
									emailMessage ->
											CompletableFuture.supplyAsync(
													() -> sendEmail(emailMessage),
													emailSendExecutor))
							.toList();

			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

			for (CompletableFuture<Boolean> future : futures) {
				try {
					if (future.get()) {
						successCount.incrementAndGet();
					} else {
						failCount.incrementAndGet();
					}
				} catch (Exception e) {
					failCount.incrementAndGet();
					log.error("이메일 전송 결과 확인 중 오류", e);
				}
			}

			if (i < batches.size() - 1) {
				try {
					Thread.sleep(BATCH_DELAY_MS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					log.warn("배치 딜레이 중 인터럽트 발생");
				}
			}
		}

		emailSendJobService.completeJob(jobId, successCount.get(), failCount.get());
		log.info(
				"비동기 이메일 병렬 발송 완료 - jobId: {}, 성공: {}, 실패: {}",
				jobId,
				successCount.get(),
				failCount.get());
	}

	/** 리스트를 지정된 크기의 배치로 분할 */
	private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
		List<List<T>> partitions = new ArrayList<>();
		for (int i = 0; i < list.size(); i += batchSize) {
			partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
		}
		return partitions;
	}
}
