package org.cotato.backend.recruit.admin.dto.response.recruitmentInformation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;

@Builder
public record RecruitmentInformationResponse(
		LocalDateTime recruitmentStart,
		LocalDateTime recruitmentEnd,
		LocalDate documentAnnouncement,
		LocalDate interviewStart,
		LocalDate interviewEnd,
		LocalDate finalAnnouncement,
		LocalDate ot) {
	public static RecruitmentInformationResponse of(List<RecruitmentInformation> informations) {
		Map<InformationType, RecruitmentInformation> infoMap =
				informations.stream()
						.collect(
								Collectors.toMap(
										RecruitmentInformation::getInformationType,
										Function.identity()));

		return RecruitmentInformationResponse.builder()
				.recruitmentStart(getDateTime(infoMap, InformationType.RECRUITMENT_START))
				.recruitmentEnd(getDateTime(infoMap, InformationType.RECRUITMENT_END))
				.documentAnnouncement(getDate(infoMap, InformationType.DOCUMENT_ANNOUNCEMENT))
				.interviewStart(getDate(infoMap, InformationType.INTERVIEW_START))
				.interviewEnd(getDate(infoMap, InformationType.INTERVIEW_END))
				.finalAnnouncement(getDate(infoMap, InformationType.FINAL_ANNOUNCEMENT))
				.ot(getDate(infoMap, InformationType.OT))
				.build();
	}

	private static LocalDateTime getDateTime(
			Map<InformationType, RecruitmentInformation> map, InformationType type) {
		RecruitmentInformation info = map.get(type);
		return info != null ? info.getEventDatetime() : null;
	}

	private static LocalDate getDate(
			Map<InformationType, RecruitmentInformation> map, InformationType type) {
		RecruitmentInformation info = map.get(type);
		return info != null ? info.getEventDatetime().toLocalDate() : null;
	}
}
