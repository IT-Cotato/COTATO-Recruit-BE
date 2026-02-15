package org.cotato.backend.recruit.common.email.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.common.email.enums.EmailHtmlTemplateType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailHtmlTemplateRenderer {

	@Value("${email.recruitment-url:https://recruit.cotato.kr}")
	private String recruitmentUrl;

	@Value("${email.kakao-channel-url:https://pf.kakao.com/_LQLyG}")
	private String kakaoChannelUrl;

	/**
	 * 모집 알림 이메일 HTML 렌더링
	 *
	 * @param content 이메일 본문 내용
	 * @param generationNumber 기수 번호
	 * @return 렌더링된 HTML
	 */
	public String renderRecruitmentNotification(String content, Long generationNumber) {
		String template = loadTemplate(EmailHtmlTemplateType.RECRUITMENT_NOTIFICATION);
		return template.replace("{{content}}", formatContent(content))
				.replace("{{generationNumber}}", String.valueOf(generationNumber))
				.replace("{{recruitmentUrl}}", recruitmentUrl)
				.replace("{{kakaoChannelUrl}}", kakaoChannelUrl);
	}

	/**
	 * 합격/불합격 결과 이메일 HTML 렌더링
	 *
	 * @param content 이메일 본문 내용
	 * @param generationNumber 기수 번호
	 * @return 렌더링된 HTML
	 */
	public String renderPassResult(String content, Long generationNumber) {
		String template = loadTemplate(EmailHtmlTemplateType.PASS_RESULT);
		return template.replace("{{content}}", formatContent(content))
				.replace("{{generationNumber}}", String.valueOf(generationNumber))
				.replace("{{kakaoChannelUrl}}", kakaoChannelUrl);
	}

	/** 템플릿 파일 로드 */
	private String loadTemplate(EmailHtmlTemplateType templateType) {
		try {
			ClassPathResource resource = new ClassPathResource(templateType.getTemplatePath());
			try (InputStream inputStream = resource.getInputStream()) {
				return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			}
		} catch (IOException e) {
			log.error("템플릿 로드 실패: {}", templateType.getTemplatePath(), e);
			throw new RuntimeException("이메일 템플릿을 로드할 수 없습니다.", e);
		}
	}

	/** 내용 포맷팅 (줄바꿈 처리) */
	private String formatContent(String content) {
		if (content == null) {
			return "";
		}
		return content.replace("\r\n", "<br>").replace("\r", "<br>").replace("\n", "<br>");
	}
}
