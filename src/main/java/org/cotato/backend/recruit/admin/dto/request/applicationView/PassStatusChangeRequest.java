package org.cotato.backend.recruit.admin.dto.request.applicationView;

import jakarta.validation.constraints.NotNull;
import org.cotato.backend.recruit.domain.application.enums.PassStatus;

public record PassStatusChangeRequest(@NotNull PassStatus passStatus) {}
