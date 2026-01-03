package org.cotato.backend.recruit.admin.service.generation;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.error.AdminErrorCode;
import org.cotato.backend.recruit.admin.exception.AdminException;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.generation.repository.GenerationRepository;
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

	@Transactional
	public Generation saveGeneration(Long generation) {
		return generationRepository.save(Generation.builder().id(generation).build());
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
}
