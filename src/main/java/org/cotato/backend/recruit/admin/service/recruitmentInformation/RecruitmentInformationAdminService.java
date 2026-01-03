package org.cotato.backend.recruit.admin.service.recruitmentInformation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.recruitmentInformation.RecruitmentInformationUpdateRequest;
import org.cotato.backend.recruit.admin.dto.response.recruitmentInformation.RecruitmentInformationResponse;
import org.cotato.backend.recruit.admin.service.generation.GenerationAdminService;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.cotato.backend.recruit.domain.recruitmentInformation.repository.RecruitmentInformationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentInformationAdminService {
	private final RecruitmentInformationRepository recruitmentInformationRepository;
	private final GenerationAdminService generationAdminService;

	public RecruitmentInformationResponse getRecruitmentInformation(Long generationId) {
		Generation generation = generationAdminService.getGenerationById(generationId);

		List<RecruitmentInformation> informations =
				recruitmentInformationRepository.findByGeneration(generation);
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

	public RecruitmentInformation getRecruitmentInformation(
			Generation generation, InformationType informationType) {
		return recruitmentInformationRepository
				.findByGenerationAndInformationType(generation, informationType)
				.orElseThrow(() -> new RuntimeException("RecruitmentInformation not found"));
	}

	@Transactional
	public void updateRecruitmentInformation(RecruitmentInformationUpdateRequest request) {
		validate(request);

		Generation generation = generationAdminService.getGenerationById(request.generation());

		upsertInformation(
				generation, InformationType.RECRUITMENT_START, request.recruitmentStart());
		upsertInformation(generation, InformationType.RECRUITMENT_END, request.recruitmentEnd());
		upsertInformation(
				generation, InformationType.DOCUMENT_ANNOUNCEMENT, request.documentAnnouncement());
		upsertInformation(generation, InformationType.INTERVIEW_START, request.interviewStart());
		upsertInformation(generation, InformationType.INTERVIEW_END, request.interviewEnd());
		upsertInformation(
				generation, InformationType.FINAL_ANNOUNCEMENT, request.finalAnnouncement());
		upsertInformation(generation, InformationType.OT, request.ot());
	}

	private LocalDateTime getDateTime(
			Map<InformationType, RecruitmentInformation> map, InformationType type) {
		RecruitmentInformation info = map.get(type);
		return info != null ? info.getEventDatetime() : null;
	}

	private LocalDate getDate(
			Map<InformationType, RecruitmentInformation> map, InformationType type) {
		RecruitmentInformation info = map.get(type);
		return info != null ? info.getEventDatetime().toLocalDate() : null;
	}

	private void upsertInformation(
			Generation generation, InformationType type, LocalDateTime datetime) {
		if (datetime == null) return;
		RecruitmentInformation info =
				recruitmentInformationRepository
						.findByGenerationAndInformationType(generation, type)
						.orElse(
								RecruitmentInformation.builder()
										.generation(generation)
										.informationType(type)
										.eventDatetime(datetime)
										.build());
		info.updateEventDatetime(datetime);
		recruitmentInformationRepository.save(info);
	}

	private void upsertInformation(Generation generation, InformationType type, LocalDate date) {
		if (date == null) return;
		upsertInformation(generation, type, date.atStartOfDay());
	}

	private void validate(RecruitmentInformationUpdateRequest request) {
		if (request.recruitmentStart() != null
				&& request.recruitmentEnd() != null
				&& request.recruitmentStart().isAfter(request.recruitmentEnd())) {
			throw new IllegalArgumentException("지원 시작일은 종료일보다 빨라야 합니다.");
		}
		if (request.interviewStart() != null
				&& request.interviewEnd() != null
				&& request.interviewStart().isAfter(request.interviewEnd())) {
			throw new IllegalArgumentException("면접 시작일은 종료일보다 빨라야 합니다.");
		}
	}
}
