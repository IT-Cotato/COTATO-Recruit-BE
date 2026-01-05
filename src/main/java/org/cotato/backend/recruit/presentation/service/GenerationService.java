package org.cotato.backend.recruit.presentation.service;

import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.generation.repository.GenerationRepository;
import org.cotato.backend.recruit.presentation.error.ApplicationErrorCode;
import org.cotato.backend.recruit.presentation.exception.ApplicationException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenerationService {

	private final GenerationRepository generationRepository;

	/**
	 * 현재 모집 중인 기수 조회 (캐시 적용) admin 서비스에서 모집 비활성화 시 캐시 만료 처리 필요
	 *
	 * @return 활성화된 기수
	 */
	@Cacheable(value = "activeGeneration", key = "'current'")
	public Generation getActiveGeneration() {

		return generationRepository
				.findByIsRecruitingActive(true)
				.orElseThrow(
						() -> new ApplicationException(ApplicationErrorCode.GENERATION_NOT_FOUND));
	}

	/**
	 * 현재 활성화된 모집 기수 ID 조회 admin 서비스에서 모집 비활성화 시 캐시 만료 처리 필요
	 *
	 * @return 활성화된 기수 ID (활성화된 기수가 없으면 null)
	 */
	@Cacheable(value = "activeGeneration", key = "'generationId'")
	public Long getActiveGenerationId() {
		return generationRepository
				.findByIsRecruitingActive(true)
				.map(Generation::getId)
				.orElse(null);
	}

	// Generation find
	/**
	 * @param generationId
	 * @return
	 */
	public Generation findGeneration(Long generationId) {
		return generationRepository
				.findById(generationId)
				.orElseThrow(
						() -> new ApplicationException(ApplicationErrorCode.GENERATION_NOT_FOUND));
	}

	// Generation save
	/**
	 * @param generationId
	 * @return
	 */
	@Transactional
	public Generation saveGeneration(Long generationId) {
		Generation generation = Generation.builder().id(generationId).build();
		generationRepository.save(generation);
		return generation;
	}
}
