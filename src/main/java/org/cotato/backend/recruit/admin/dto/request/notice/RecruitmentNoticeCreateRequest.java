package org.cotato.backend.recruit.admin.dto.request.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.cotato.backend.recruit.admin.dto.request.notice.enums.ActivityType;
import org.cotato.backend.recruit.admin.dto.request.notice.enums.RecruitmentPartType;
import org.cotato.backend.recruit.admin.dto.request.notice.enums.ScheduleType;

@Schema(description = "모집 공고 생성 요청")
public record RecruitmentNoticeCreateRequest(
		@Schema(description = "기수 번호", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
				@NotNull(message = "기수는 필수입니다.")
				Long generationId,
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
							description = "파트 타입 (PM, DE, FE, BE)",
							example = "PM",
							requiredMode = Schema.RequiredMode.REQUIRED)
					@NotNull(message = "파트 타입은 필수입니다.")
					RecruitmentPartType partType,
			@Schema(
							description = "파트 상세 설명",
							example = "프로젝트의 기획과 관리를 담당하는 기획 파트입니다.",
							requiredMode = Schema.RequiredMode.REQUIRED)
					@NotNull(message = "파트 상세 설명은 필수입니다.")
					String detail) {}

	@Schema(description = "주요 활동 요청")
	public record ActivityRequest(
			@Schema(
							description =
									"활동 타입 (OT, REGULAR_SESSION, MT, DEV_TALK, COKATHON, DEMO_DAY)",
							example = "OT",
							requiredMode = Schema.RequiredMode.REQUIRED)
					@NotNull(message = "활동 타입은 필수입니다.")
					ActivityType activityType,
			@Schema(
							description = "활동 일정",
							example = "2025.9.26",
							requiredMode = Schema.RequiredMode.REQUIRED)
					@NotNull(message = "활동 일정은 필수입니다.")
					String date) {}

	@Schema(description = "모집 일정 요청")
	public record ScheduleRequest(
			@Schema(
							description =
									"일정 타입 (APPLICATION, DOCUMENT_RESULT, INTERVIEW, FINAL_RESULT,"
											+ " OT)",
							example = "APPLICATION",
							requiredMode = Schema.RequiredMode.REQUIRED)
					@NotNull(message = "일정 타입은 필수입니다.")
					ScheduleType scheduleType,
			@Schema(
							description = "일정 날짜",
							example = "2월 20일 (금) ~ \n 2월 27일 (금) \n 오후 11시 59분",
							requiredMode = Schema.RequiredMode.REQUIRED)
					@NotNull(message = "일정 날짜는 필수입니다.")
					String date) {}
}
