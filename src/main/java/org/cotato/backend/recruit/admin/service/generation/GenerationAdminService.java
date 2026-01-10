package org.cotato.backend.recruit.admin.service.generation;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.response.applicationView.GenerationElementResponse;
import org.cotato.backend.recruit.admin.error.AdminErrorCode;
import org.cotato.backend.recruit.admin.error.ApplicationAdminErrorCode;
import org.cotato.backend.recruit.admin.exception.AdminException;
import org.cotato.backend.recruit.admin.exception.ApplicationAdminException;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.generation.repository.GenerationRepository;
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
				.orElseThrow(
						() ->
								new ApplicationAdminException(
										ApplicationAdminErrorCode.GENERATION_NOT_FOUND));
	}

	public Optional<Generation> findGenerationOptional(Long generation) {
		return generationRepository.findById(generation);
	}

	@Transactional
	public Generation saveNewGenerationWithRecruitingActive(Long generation) {
		return generationRepository.save(
				Generation.builder().id(generation).isRecruitingActive(true).build());
	}

	public List<GenerationElementResponse> getAllGenerations() {
		return generationRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
				.map(GenerationElementResponse::from)
				.toList();
	}
}
