package org.cotato.backend.recruit.admin.service.generationAdmin;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.error.ApplicationAdminErrorCode;
import org.cotato.backend.recruit.admin.exception.ApplicationAdminException;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.generation.repository.GenerationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenerationAdminService {
	private final GenerationRepository generationRepository;

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

	public Generation saveGeneration(Long generation) {
		return generationRepository.save(
				Generation.builder().id(generation).isRecruitingActive(true).build());
	}
}
