package org.cotato.backend.recruit.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.cotato.backend.recruit.domain.application.enums.DiscoveryPath;

@Schema(description = "기타 질문 답변 및 추가 정보 요청")
public record EtcAnswersRequest(
		@Schema(
						description = "알게 된 경로",
						example = "SNS",
						allowableValues = {"SNS", "FRIEND_REFERRAL", "SCHOOL_PROMOTION", "OTHER"},
						requiredMode = Schema.RequiredMode.REQUIRED)
				@NotNull(message = "알게 된 경로는 필수입니다.")
				DiscoveryPath discoveryPath,
		@Schema(description = "병행 활동 (600자 이내)", example = "다른 동아리 활동 중입니다.")
				@Size(max = 600, message = "병행 활동은 600자를 초과할 수 없습니다.")
				String parallelActivities,
		@Schema(description = "면접 불가능 시간대", example = "3월 3일 14:00, 3월 4일 10:00")
				String unavailableInterviewTimes,
		@Schema(
						description = "세션 출석 동의",
						example = "true",
						requiredMode = Schema.RequiredMode.REQUIRED)
				@NotNull(message = "세션 출석 동의는 필수입니다.")
				Boolean sessionAttendanceAgreed,
		@Schema(
						description = "필수 행사 참여 동의",
						example = "true",
						requiredMode = Schema.RequiredMode.REQUIRED)
				@NotNull(message = "필수 행사 참여 동의는 필수입니다.")
				Boolean mandatoryEventsAgreed,
		@Schema(
						description = "개인정보 처리 동의",
						example = "true",
						requiredMode = Schema.RequiredMode.REQUIRED)
				@NotNull(message = "개인정보 처리 동의는 필수입니다.")
				Boolean privacyPolicyAgreed) {}
