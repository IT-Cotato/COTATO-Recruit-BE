package org.cotato.backend.recruit.admin.dto.response.applicationView;

import java.util.List;
import lombok.Builder;
import org.cotato.backend.recruit.admin.dto.response.recruitmentInformation.RecruitmentPeriodResponse;

@Builder
public record AdminApplicationsResponse(
		RecruitmentPeriodResponse recruitmentPeriodResponse,
		ApplicationSummaryResponse summary,
		Applicants applicants) {
	@Builder
	public record Applicants(List<ApplicationElementResponse> content, PageInfoResponse pageInfo) {}
}
