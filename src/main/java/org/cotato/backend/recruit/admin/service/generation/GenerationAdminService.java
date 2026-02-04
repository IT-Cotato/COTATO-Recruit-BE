package org.cotato.backend.recruit.admin.service.generation;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.response.generation.GenerationElementResponse;
import org.cotato.backend.recruit.admin.error.AdminErrorCode;
import org.cotato.backend.recruit.admin.exception.AdminException;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.generation.repository.GenerationRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenerationAdminService {

	private final GenerationRepository generationRepository;

	public Generation getGenerationById(Long generationId) {
		return generationRepository
				.findById(generationId)
				.orElseThrow(() -> new AdminException(AdminErrorCode.GENERATION_NOT_FOUND));
	}

	// -------------------------------------------------------------------------
	// 현재 활동 기수 관련 로직
	// -------------------------------------------------------------------------

	/** 현재 모집 중인 기수 조회 */
	public Optional<Generation> findActiveGeneration() {
		return generationRepository.findByIsRecruitingActive(true);
	}

	/** 현재 모집 중인 기수 조회 (없으면 예외 발생) */
	public Generation getActiveGeneration() {
		return generationRepository
				.findByIsRecruitingActive(true)
				.orElseThrow(() -> new AdminException(AdminErrorCode.NO_ACTIVE_GENERATION));
	}

	/** 기수 조회 (generationId가 null이면 현재 활성화된 기수 반환) */
	public Generation getGenerationOrActive(Long generationId) {
		return generationId != null ? getGenerationById(generationId) : getActiveGeneration();
	}

	// PK(generationID)와 generation이 동일해서 findGeneration으로 검색하면됌
	// Long generation은 PK, 기수 둘다 가능
	public Generation findGeneration(Long generation) {
		return generationRepository
				.findById(generation)
				.orElseThrow(() -> new AdminException(AdminErrorCode.GENERATION_NOT_FOUND));
	}

	public Optional<Generation> findGenerationOptional(Long generation) {
		return generationRepository.findById(generation);
	}

	// 모집활성화된 기수를 생성, 추가모집활성화여부는 false
	@Transactional
	@CacheEvict(
			value = {"latestGeneration", "activeGeneration"},
			allEntries = true)
	public Generation saveNewGenerationWithRecruitingActive(Long generation) {
		return generationRepository.save(new Generation(generation, true, false));
	}

	public List<GenerationElementResponse> getAllGenerations() {
		return generationRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
				.map(GenerationElementResponse::from)
				.toList();
	}

	// 모집활성화여부 false, 추가모집활성화여부는 false
	@Transactional
	@CacheEvict(
			value = {"latestGeneration", "activeGeneration"},
			allEntries = true)
	public Generation createGeneration(Long generation) {
		boolean existsGeneration = generationRepository.existsById(generation);
		if (existsGeneration) {
			throw new AdminException(AdminErrorCode.GENERATION_ALREADY_EXISTS);
		}
		return generationRepository.save(new Generation(generation, false, false));
	}
}
