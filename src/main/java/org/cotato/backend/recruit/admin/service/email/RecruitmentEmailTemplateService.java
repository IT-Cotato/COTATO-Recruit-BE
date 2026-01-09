package org.cotato.backend.recruit.admin.service.email;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cotato.backend.recruit.admin.dto.response.email.RecruitmentEmailTemplateResponse;
import org.cotato.backend.recruit.admin.service.generation.GenerationAdminService;
import org.cotato.backend.recruit.domain.email.entity.RecruitmentEmailTemplate;
import org.cotato.backend.recruit.domain.email.repository.RecruitmentEmailTemplateRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.subscriber.repository.RecruitmentSubscriberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentEmailTemplateService {

	private final RecruitmentEmailTemplateRepository recruitmentEmailTemplateRepository;
	private final RecruitmentSubscriberRepository recruitmentSubscriberRepository;
	private final GenerationAdminService generationAdminService;

	/** 모집 알림 메일 템플릿 조회 (구독자 수 포함) */
	@Transactional
	public RecruitmentEmailTemplateResponse getRecruitmentEmailTemplate(Long generationId) {
		// generationId가 null이면 현재 활성화된 기수를 조회하되, 없으면 빈 응답 반환
		Optional<Generation> generationOpt =
				generationId != null
						? Optional.of(generationAdminService.getGenerationById(generationId))
						: generationAdminService.findActiveGeneration();

		if (generationOpt.isEmpty()) {
			return RecruitmentEmailTemplateResponse.empty();
		}

		Generation generation = generationOpt.get();
		RecruitmentEmailTemplate template = getOrCreateTemplate(generation);

		// 아직 알림을 받지 않은 구독자 수 조회
		long subscriberCount = recruitmentSubscriberRepository.countByIsNotified(false);

		return RecruitmentEmailTemplateResponse.of(template, subscriberCount);
	}

	/** 모집 알림 메일 내용 저장 (존재하면 수정, 없으면 생성) */
	@Transactional
	public void saveRecruitmentEmailContent(Long generationId, String content) {
		Generation generation = generationAdminService.getGenerationOrActive(generationId);

		// 존재하면 조회, 없으면 생성
		RecruitmentEmailTemplate template = getOrCreateTemplate(generation);

		// 이미 전송된 메일인지 확인
		template.validateNotSent();

		// 내용 저장 (생성/수정)
		template.updateContent(content);

		recruitmentEmailTemplateRepository.save(template);
	}

	/** 모집 알림 메일 템플릿 조회 또는 생성 */
	@Transactional
	public RecruitmentEmailTemplate getOrCreateTemplate(Generation generation) {
		return recruitmentEmailTemplateRepository
				.findByGeneration(generation)
				.orElseGet(
						() -> {
							RecruitmentEmailTemplate newTemplate =
									RecruitmentEmailTemplate.builder()
											.generation(generation)
											.content("") // 빈 content로 시작
											.build();
							return recruitmentEmailTemplateRepository.save(newTemplate);
						});
	}
}
