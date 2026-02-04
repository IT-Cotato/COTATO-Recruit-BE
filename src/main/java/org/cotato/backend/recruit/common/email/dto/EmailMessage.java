package org.cotato.backend.recruit.common.email.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailMessage {
	private String to; // 수신자 이메일
	private String subject; // 제목
	private String content; // 내용 (HTML)

	@Builder.Default private boolean withTemplateImages = false; // 템플릿 이미지 첨부 여부
}
