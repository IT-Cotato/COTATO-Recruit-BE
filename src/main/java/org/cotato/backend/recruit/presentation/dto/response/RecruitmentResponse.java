package org.cotato.backend.recruit.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"isActive",
	"generation",
	"startDate",
	"endDate",
	"schedule",
	"parts",
	"activities"
})
public class RecruitmentResponse {
	private boolean isActive;
	private int generation;
	private String startDate;
	private String endDate;
	private List<ScheduleResponse> schedule;
	private List<PartResponse> parts;
	private List<ActivityResponse> activities;

	@Getter
	@Builder
	public static class ScheduleResponse {
		private String title;
		private String date;
	}

	@Getter
	@Builder
	public static class PartResponse {
		private String name;

		@JsonProperty("short")
		private String partShort;

		private String detail;
	}

	@Getter
	@Builder
	public static class ActivityResponse {
		private Long id;
		private String name;
		private String date;
	}
}
