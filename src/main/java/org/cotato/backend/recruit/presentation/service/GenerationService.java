package org.cotato.backend.recruit.presentation.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.generation.repository.GenerationRepository;
import org.cotato.backend.recruit.presentation.error.PresentationErrorCode;
import org.cotato.backend.recruit.presentation.exception.PresentationException;
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
	 * @throws PresentationException 활성화된 기수가 없거나 2개 이상일 때
	 */
	@Cacheable(value = "activeGeneration", key = "'current'")
	public Generation getActiveGeneration() {
		// 활성화된 기수가 2개 이상일때 예외처리
		List<Generation> activeGenerations = generationRepository.findAllByIsRecruitingActive(true);

		if (activeGenerations.isEmpty()) {
			throw new PresentationException(PresentationErrorCode.GENERATION_NOT_FOUND);
		}

		if (activeGenerations.size() > 1) {
			throw new PresentationException(PresentationErrorCode.GENERATION_MULTIPLE_ACTIVE);
		}

		return activeGenerations.get(0);
	}

	/**
	 * 현재 활성화된 모집 기수 ID 조회 admin 서비스에서 모집 비활성화 시 캐시 만료 처리 필요
	 *
	 * @return Optional<Generation>
	 * @throws PresentationException 활성화된 기수가 2개 이상일 때
	 */
	@Cacheable(value = "activeGeneration", key = "'generationId'")
	public Optional<Generation> getActiveGenerationOptional() {
		List<Generation> activeGenerations = generationRepository.findAllByIsRecruitingActive(true);

		if (activeGenerations.size() > 1) {
			throw new PresentationException(PresentationErrorCode.GENERATION_MULTIPLE_ACTIVE);
		}

		return activeGenerations.isEmpty() ? Optional.empty() : Optional.of(activeGenerations.get(0));
	}

	/**
	 * 최신 기수 조회 (활성화 여부와 관계없이 id 기준으로 가장 최근 기수)
	 *
	 * @return 최신 기수
	 */
	@Cacheable(value = "latestGeneration", key = "'current'")
	public Generation getLatestGeneration() {
		return generationRepository
				.findFirstByOrderByIdDesc()
				.orElseThrow(
						() -> new PresentationException(
								PresentationErrorCode.GENERATION_NOT_FOUND));
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
						() -> new PresentationException(
								PresentationErrorCode.GENERATION_NOT_FOUND));
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
