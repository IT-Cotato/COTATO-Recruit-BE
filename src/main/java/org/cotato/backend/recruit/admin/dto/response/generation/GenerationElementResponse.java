package org.cotato.backend.recruit.admin.dto.response.generation;

import org.cotato.backend.recruit.domain.generation.entity.Generation;

public record GenerationElementResponse(Long generationId) {
	public static GenerationElementResponse from(Generation generation) {
		return new GenerationElementResponse(generation.getId());
	}
}
