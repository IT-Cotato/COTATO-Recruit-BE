package org.cotato.backend.recruit.admin.dto.request.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "모집 공고 생성 요청")
public record RecruitmentNoticeCreateRequest(
		@Schema(description = "기수 번호", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
				@NotNull(message = "기수는 필수입니다.")
				Integer generation,
		@Schema(
						description = "모집 파트 목록 (PM, DE, FE, BE 순서)",
						requiredMode = Schema.RequiredMode.REQUIRED)
				@NotEmpty(message = "모집 파트는 필수입니다.")
				@Size(min = 4, max = 4, message = "모집 파트는 4개여야 합니다.")
				@Valid
				List<PartRequest> parts,
		@Schema(
						description = "주요 활동 목록 (OT, 정기 세션, MT, DevTalk, 코커톤, 데모데이 순서)",
						requiredMode = Schema.RequiredMode.REQUIRED)
				@NotEmpty(message = "주요 활동은 필수입니다.")
				@Size(min = 6, max = 6, message = "주요 활동은 6개여야 합니다.")
				@Valid
				List<ActivityRequest> activities,
		@Schema(
						description = "모집 일정 목록 (서류 접수, 서류 합격 발표, 면접 진행, 최종 합격 발표, OT 순서)",
						requiredMode = Schema.RequiredMode.REQUIRED)
				@NotEmpty(message = "모집 일정은 필수입니다.")
				@Size(min = 5, max = 5, message = "모집 일정은 5개여야 합니다.")
				@Valid
				List<ScheduleRequest> schedules) {

	@Schema(description = "모집 파트 요청")
	public record PartRequest(
			@Schema(
							description = "파트명",
							example = "PM",
							requiredMode = Schema.RequiredMode.REQUIRED)
					@NotNull(message = "파트명은 필수입니다.")
					String name,
			@Schema(
							description = "파트 짧은 설명",
							example = "기획",
							requiredMode = Schema.RequiredMode.REQUIRED)
					@NotNull(message = "파트 짧은 설명은 필수입니다.")
					String partShort,
			@Schema(
							description = "파트 상세 설명",
							example = "서비스 기획 및 전략 수립",
							requiredMode = Schema.RequiredMode.REQUIRED)
					@NotNull(message = "파트 상세 설명은 필수입니다.")
					String detail) {}

	@Schema(description = "주요 활동 요청")
	public record ActivityRequest(
			@Schema(
							description = "활동명",
							example = "OT",
							requiredMode = Schema.RequiredMode.REQUIRED)
					@NotNull(message = "활동명은 필수입니다.")
					String name,
			@Schema(
							description = "활동 일정",
							example = "3월 첫째 주",
							requiredMode = Schema.RequiredMode.REQUIRED)
					@NotNull(message = "활동 일정은 필수입니다.")
					String date) {}

	@Schema(description = "모집 일정 요청")
	public record ScheduleRequest(
			@Schema(
							description = "일정 제목",
							example = "서류 접수",
							requiredMode = Schema.RequiredMode.REQUIRED)
					@NotNull(message = "일정 제목은 필수입니다.")
					String title,
			@Schema(
							description = "일정 날짜",
							example = "2024.02.01 - 2024.02.15",
							requiredMode = Schema.RequiredMode.REQUIRED)
					@NotNull(message = "일정 날짜는 필수입니다.")
					String date) {}
}
