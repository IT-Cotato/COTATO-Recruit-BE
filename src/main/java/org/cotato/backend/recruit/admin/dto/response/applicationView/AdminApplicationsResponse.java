package org.cotato.backend.recruit.admin.dto.response.applicationView;

import java.util.List;
import lombok.Builder;
import org.cotato.backend.recruit.admin.dto.response.recruitmentInformation.RecruitmentPeriodResponse;

@Builder
public record AdminApplicationsResponse(
		RecruitmentPeriodResponse recruitmentPeriodResponse,
		ApplicationSummaryResponse summary,
		Applicants applicants) {

	public static AdminApplicationsResponse of(
			RecruitmentInformationResponse recruitmentInformationResponse,
			ApplicationSummaryResponse summary,
			Applicants applicants) {
		return AdminApplicationsResponse.builder()
				.recruitmentInformationResponse(recruitmentInformationResponse)
				.summary(summary)
				.applicants(applicants)
				.build();
	}

	@Builder
	public record Applicants(List<ApplicationElementResponse> content, PageInfoResponse pageInfo) {
		public static Applicants of(
				List<ApplicationElementResponse> content, PageInfoResponse pageInfo) {
			return Applicants.builder().content(content).pageInfo(pageInfo).build();
		}
	}
}
