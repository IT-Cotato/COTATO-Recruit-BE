package org.cotato.backend.recruit.admin.dto.response.applicationView;

import lombok.Builder;

@Builder
public record ApplicationSummaryResponse(
		long totalCount, long pmCount, long designCount, long frontendCount, long backendCount) {}
