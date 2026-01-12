package org.cotato.backend.recruit.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import org.cotato.backend.recruit.domain.recruitmentNotice.entity.RecruitmentNotice;

@JsonPropertyOrder({"generation", "startDate", "endDate", "schedule", "parts", "activities"})
public record RecruitmentResponse(
		Long generationId,
		String startDate,
		String endDate,
		List<ScheduleResponse> schedule,
		List<PartResponse> parts,
		List<ActivityResponse> activities) {
	public record ScheduleResponse(String title, String date) {
		public static ScheduleResponse from(RecruitmentNotice n) {
			return new ScheduleResponse(n.getScheduleTitle(), n.getSchedule());
		}
	}

	public record PartResponse(
			String name, @JsonProperty("short") String partShort, String detail, String imageUrl) {
		public static PartResponse from(RecruitmentNotice n) {
			String imageUrl =
					n.getImageFilename() != null
							? "/backend/images/parts/" + n.getImageFilename()
							: null;
			return new PartResponse(n.getPartName(), n.getPartShort(), n.getPartDetail(), imageUrl);
		}
	}

	public record ActivityResponse(Long id, String name, String date, String imageUrl) {
		public static ActivityResponse from(RecruitmentNotice n) {
			String imageUrl =
					n.getImageFilename() != null
							? "/backend/images/activities/" + n.getImageFilename()
							: null;
			return new ActivityResponse(n.getId(), n.getScheduleTitle(), n.getSchedule(), imageUrl);
		}
	}
}
