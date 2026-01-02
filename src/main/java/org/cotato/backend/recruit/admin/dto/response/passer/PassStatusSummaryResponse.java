package org.cotato.backend.recruit.admin.dto.response.passer;

import java.util.Map;
import lombok.Builder;
import org.cotato.backend.recruit.domain.application.enums.PassStatus;

@Builder
public record PassStatusSummaryResponse(PassStatus passStatus, Map<String, Long> counts) {}
