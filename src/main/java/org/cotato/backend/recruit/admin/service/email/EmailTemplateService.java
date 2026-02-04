package org.cotato.backend.recruit.admin.service.email;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.admin.dto.response.email.EmailTemplateResponse;
import org.cotato.backend.recruit.admin.service.application.ApplicationAdminService;
import org.cotato.backend.recruit.admin.service.generation.GenerationAdminService;
import org.cotato.backend.recruit.domain.application.enums.PassStatus;
import org.cotato.backend.recruit.domain.email.entity.EmailSendJob;
import org.cotato.backend.recruit.domain.email.entity.EmailTemplate;
import org.cotato.backend.recruit.domain.email.enums.EmailJobType;
import org.cotato.backend.recruit.domain.email.enums.TemplateType;
import org.cotato.backend.recruit.domain.email.repository.EmailSendJobRepository;
import org.cotato.backend.recruit.domain.email.repository.EmailTemplateRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailTemplateService {

	private final EmailTemplateRepository emailTemplateRepository;
	private final EmailSendJobRepository emailSendJobRepository;
	private final ApplicationAdminService applicationAdminService;
	private final GenerationAdminService generationAdminService;

	/** 메일 템플릿 조회 (대상자 수 포함) */
	@Transactional
	public EmailTemplateResponse getEmailTemplate(TemplateType templateType, Long generationId) {
		// generationId가 null이면 현재 활성화된 기수를 조회하되, 없으면 빈 응답 반환
		Optional<Generation> generationOpt =
				generationId != null
						? Optional.of(generationAdminService.getGenerationById(generationId))
						: generationAdminService.findActiveGeneration();

		if (generationOpt.isEmpty()) {
			return EmailTemplateResponse.empty(templateType.name(), templateType.getDescription());
		}

		Generation generation = generationOpt.get();
		PassStatus passStatus = templateType.toPassStatus();

		EmailTemplate emailTemplate = getOrCreateEmailTemplate(generation, templateType);

		// 대상자 수 조회
		long recipientCount =
				applicationAdminService.countByGenerationAndPassStatus(generation, passStatus);

		// Job 정보 조회 (가장 최근 것)
		EmailSendJob job =
				emailSendJobRepository
						.findTopByGenerationAndJobTypeOrderByCreatedAtDesc(
								generation, EmailJobType.from(templateType))
						.orElse(null);

		return EmailTemplateResponse.of(emailTemplate, recipientCount, job);
	}

	/** 메일 내용 저장 (존재하면 수정, 없으면 생성) */
	@Transactional
	public void saveEmailContent(TemplateType templateType, Long generationId, String content) {
		Generation generation = generationAdminService.getGenerationOrActive(generationId);

		// 존재하면 조회, 없으면 생성
		EmailTemplate emailTemplate = getOrCreateEmailTemplate(generation, templateType);

		// 이미 전송된 메일인지 확인
		emailTemplate.validateNotSent();

		// 내용 저장 (생성/수정)
		emailTemplate.updateContent(content);

		emailTemplateRepository.save(emailTemplate);
	}

	/** 메일 템플릿 조회 또는 생성 */
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
							return emailTemplateRepository.save(newTemplate);
						});
	}
}
