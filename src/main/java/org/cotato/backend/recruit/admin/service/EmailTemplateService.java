package org.cotato.backend.recruit.admin.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.admin.dto.request.EmailContentUpdateRequest;
import org.cotato.backend.recruit.admin.dto.response.EmailTemplateResponse;
import org.cotato.backend.recruit.admin.error.EmailAdminErrorCode;
import org.cotato.backend.recruit.admin.exception.EmailAdminException;
import org.cotato.backend.recruit.domain.application.enums.PassStatus;
import org.cotato.backend.recruit.domain.application.repository.ApplicationRepository;
import org.cotato.backend.recruit.domain.email.entity.EmailTemplate;
import org.cotato.backend.recruit.domain.email.enums.TemplateType;
import org.cotato.backend.recruit.domain.email.repository.EmailTemplateRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.generation.repository.GenerationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailTemplateService {

	private final EmailTemplateRepository emailTemplateRepository;
	private final ApplicationRepository applicationRepository;
	private final GenerationRepository generationRepository;

	/**
	 * 메일 템플릿 조회 (대상자 수 포함)
	 *
	 * @param templateType 템플릿 타입
	 * @return 이메일 템플릿 응답 (대상자 수 포함), 모집 중인 기수가 없으면 빈 응답
	 */
	@Transactional
	public EmailTemplateResponse getEmailTemplate(TemplateType templateType) {
		Optional<Generation> activeGeneration = generationRepository.findByIsRecruitingActive(true);

		// 모집 중인 기수가 없으면 빈 응답 반환
		if (activeGeneration.isEmpty()) {
			return EmailTemplateResponse.empty(templateType.name(), templateType.getDescription());
		}

		Generation generation = activeGeneration.get();
		PassStatus passStatus = templateType.toPassStatus();

		EmailTemplate emailTemplate = getOrCreateEmailTemplate(generation, templateType);

		// 대상자 수 조회
		long recipientCount =
				applicationRepository.countByGenerationAndPassStatus(generation, passStatus);

		return EmailTemplateResponse.of(emailTemplate, recipientCount);
	}

	/**
	 * 메일 내용 수정
	 *
	 * @param templateType 템플릿 타입
	 * @param request 메일 내용 수정 요청
	 * @throws EmailAdminException 모집 중인 기수가 없는 경우
	 */
	@Transactional
	public void updateEmailContent(TemplateType templateType, EmailContentUpdateRequest request) {
		Generation activeGeneration =
				generationRepository
						.findByIsRecruitingActive(true)
						.orElseThrow(
								() ->
										new EmailAdminException(
												EmailAdminErrorCode.NO_ACTIVE_GENERATION));

		EmailTemplate emailTemplate = getOrCreateEmailTemplate(activeGeneration, templateType);

		// 이미 전송된 메일인지 확인
		emailTemplate.validateNotSent();

		// 내용 수정
		emailTemplate.updateContent(request.content());

		emailTemplateRepository.save(emailTemplate);
	}

	/**
	 * 메일 템플릿 조회 또는 생성
	 *
	 * @param generation 기수
	 * @param templateType 템플릿 타입
	 * @return 이메일 템플릿
	 */
	@Transactional
	public EmailTemplate getOrCreateEmailTemplate(
			Generation generation, TemplateType templateType) {
		return emailTemplateRepository
				.findByGenerationAndTemplateType(generation, templateType)
				.orElseGet(
						() -> {
							EmailTemplate newTemplate =
									EmailTemplate.builder()
											.generation(generation)
											.templateType(templateType)
											.content("") // 빈 content로 시작
											.build();
							EmailTemplate saved = emailTemplateRepository.save(newTemplate);

							return saved;
						});
	}
}
