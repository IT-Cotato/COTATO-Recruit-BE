package org.cotato.backend.recruit.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import org.cotato.backend.recruit.domain.recruitmentNotice.entity.RecruitmentNotice;

@JsonPropertyOrder({
	"isActive",
	"generation",
	"startDate",
	"endDate",
	"schedule",
	"parts",
	"activities"
})
public record RecruitmentResponse(
		boolean isActive,
		int generation,
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
			String name, @JsonProperty("short") String partShort, String detail) {
		public static PartResponse from(RecruitmentNotice n) {
			return new PartResponse(n.getPartName(), n.getPartShort(), n.getPartDetail());
		}
	}

	public record ActivityResponse(Long id, String name, String date) {
		public static ActivityResponse from(RecruitmentNotice n) {
			return new ActivityResponse(n.getId(), n.getScheduleTitle(), n.getSchedule());
		}
	}
}
