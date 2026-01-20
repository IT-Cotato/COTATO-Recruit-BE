package org.cotato.backend.recruit.admin.dto.request.generation;

import jakarta.validation.constraints.NotNull;

public record GenerationCreateRequest(@NotNull Long generationId) {
}
