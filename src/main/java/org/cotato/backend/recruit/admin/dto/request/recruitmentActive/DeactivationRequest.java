package org.cotato.backend.recruit.admin.dto.request.recruitmentActive;

import jakarta.validation.constraints.NotNull;

public record DeactivationRequest(@NotNull Long generation) {
}
