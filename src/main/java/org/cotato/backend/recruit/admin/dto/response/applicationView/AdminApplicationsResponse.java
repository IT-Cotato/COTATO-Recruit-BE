package org.cotato.backend.recruit.admin.dto.response.applicationView;

import java.util.List;
import lombok.Builder;

@Builder
public record AdminApplicationsResponse(
		RecruitmentInformationResponse recruitmentInformationResponse,
		ApplicationSummaryResponse summary,
		Applicants applicants) {
	@Builder
	public record Applicants(List<ApplicationElementResponse> content, PageInfoResponse pageInfo) {}
}
