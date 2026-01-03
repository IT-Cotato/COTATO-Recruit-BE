package org.cotato.backend.recruit.admin.dto.response.applicationView;

import java.util.List;
import lombok.Builder;

@Builder
public record ApplicationSummaryResponse(
		long totalCount, long pmCount, long designCount, long frontendCount, long backendCount) {

	public static ApplicationSummaryResponse from(List<Object[]> counts) {
		long pmCount = 0;
		long designCount = 0;
		long frontendCount = 0;
		long backendCount = 0;
		long totalCount = 0;

		for (Object[] row : counts) {
			String typeStr = (String) row[0];
			long count = Long.parseLong(String.valueOf(row[1]));

			totalCount += count;

			switch (typeStr) {
				case "PM" -> pmCount = count;
				case "DE" -> designCount = count;
				case "FE" -> frontendCount = count;
				case "BE" -> backendCount = count;
			}
		}

		return ApplicationSummaryResponse.builder()
				.totalCount(totalCount)
				.pmCount(pmCount)
				.designCount(designCount)
				.frontendCount(frontendCount)
				.backendCount(backendCount)
				.build();
	}
}
