package org.cotato.backend.recruit.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;

@Schema(description = "모집 일정 응답")
public record RecruitmentScheduleResponse(
		@Schema(description = "기수 ID", example = "13") Long generationId,
		@Schema(description = "지원 시작일자", example = "2025-01-01T00:00:00")
				LocalDateTime applicationStartDate,
		@Schema(description = "지원 종료일자", example = "2025-01-07T23:59:00")
				LocalDateTime applicationEndDate,
		@Schema(description = "서류 발표일자", example = "2025-01-10T18:00:00")
				LocalDateTime documentAnnouncement,
		@Schema(description = "면접 평가 시작일자", example = "2025-01-13T10:00:00")
				LocalDateTime interviewStartDate,
		@Schema(description = "면접 평가 종료일자", example = "2025-01-14T18:00:00")
				LocalDateTime interviewEndDate,
		@Schema(description = "최종 발표일자", example = "2025-01-17T18:00:00")
				LocalDateTime finalAnnouncement,
		@Schema(description = "OT 일자", example = "2025-01-20T14:00:00") LocalDateTime otDate,
		@Schema(description = "코커톤 일자", example = "2025-01-20") LocalDate cokerthonDate,
		@Schema(description = "데모데이 일자", example = "2025-01-20") LocalDate demoDayDate) {

	public static RecruitmentScheduleResponse of(
			Long generationId, Map<InformationType, LocalDateTime> scheduleMap) {
		return new RecruitmentScheduleResponse(
				generationId,
				scheduleMap.get(InformationType.RECRUITMENT_START),
				scheduleMap.get(InformationType.RECRUITMENT_END),
				scheduleMap.get(InformationType.DOCUMENT_ANNOUNCEMENT),
				scheduleMap.get(InformationType.INTERVIEW_START),
				scheduleMap.get(InformationType.INTERVIEW_END),
				scheduleMap.get(InformationType.FINAL_ANNOUNCEMENT),
				scheduleMap.get(InformationType.OT),
				scheduleMap.get(InformationType.COKERTHON) != null
						? scheduleMap.get(InformationType.COKERTHON).toLocalDate()
						: null,
				scheduleMap.get(InformationType.DEMO_DAY) != null
						? scheduleMap.get(InformationType.DEMO_DAY).toLocalDate()
						: null);
	}
}
