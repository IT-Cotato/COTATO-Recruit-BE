package org.cotato.backend.recruit.common.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * 메일 설정 클래스
 *
 * <p>JavaMailSender 빈을 명시적으로 정의하여 이메일 전송 기능을 제공합니다.
 */
@Configuration
public class MailConfig {

	@Value("${spring.mail.host}")
	private String host;

	@Value("${spring.mail.port}")
	private int port;

	@Value("${spring.mail.username}")
	private String username;

	@Value("${spring.mail.password}")
	private String password;

	@Value("${spring.mail.properties.mail.smtp.auth}")
	private boolean auth;

	@Value("${spring.mail.properties.mail.smtp.starttls.enable}")
	private boolean starttlsEnable;

	@Value("${spring.mail.properties.mail.smtp.starttls.required}")
	private boolean starttlsRequired;

	@Value("${spring.mail.properties.mail.smtp.connectiontimeout}")
	private int connectionTimeout;

	@Value("${spring.mail.properties.mail.smtp.timeout}")
	private int timeout;

	@Value("${spring.mail.properties.mail.smtp.writetimeout}")
	private int writeTimeout;

	@Bean
	public JavaMailSender javaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		mailSender.setHost(host);
		mailSender.setPort(port);
		mailSender.setUsername(username);
		mailSender.setPassword(password);

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.smtp.auth", auth);
		props.put("mail.smtp.starttls.enable", starttlsEnable);
		props.put("mail.smtp.starttls.required", starttlsRequired);
		props.put("mail.smtp.connectiontimeout", connectionTimeout);
		props.put("mail.smtp.timeout", timeout);
		props.put("mail.smtp.writetimeout", writeTimeout);

		return mailSender;
	}
}
