package org.cotato.backend.recruit.common.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.admin.error.AdminErrorCode;
import org.cotato.backend.recruit.admin.exception.AdminException;
import org.cotato.backend.recruit.common.email.dto.EmailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender mailSender;

	/**
	 * 이메일 전송
	 *
	 * @param emailMessage 이메일 메시지
	 * @throws AdminException 메일 전송 실패 시
	 */
	public void sendEmail(EmailMessage emailMessage) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setTo(emailMessage.getTo());
			helper.setSubject(emailMessage.getSubject());
			helper.setText(emailMessage.getContent(), true); // HTML 형식

			mailSender.send(message);
			log.info("이메일 전송 성공: {}", emailMessage.getTo());

		} catch (MessagingException e) {
			log.error("이메일 전송 실패: {}", emailMessage.getTo(), e);
			throw new AdminException(
					AdminErrorCode.EMAIL_SEND_FAILED,
					"이메일 전송 중 오류가 발생했습니다: " + emailMessage.getTo());
		}
	}

	/**
	 * 여러 이메일을 배치로 전송
	 *
	 * @param emailMessages 이메일 메시지 리스트
	 * @return 성공한 개수
	 */
	public long sendBatchEmails(List<EmailMessage> emailMessages) {
		long successCount = 0;

		for (EmailMessage emailMessage : emailMessages) {
			try {
				sendEmail(emailMessage);
				successCount++;
			} catch (AdminException e) {
				log.error("개별 이메일 전송 실패: {}", emailMessage.getTo());
				// 계속 진행
			}
		}

		return successCount;
	}
}
