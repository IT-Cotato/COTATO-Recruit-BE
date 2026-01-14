package org.cotato.backend.recruit.admin.service.recruitmentInformation;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.recruitmentInformation.RecruitmentInformationUpdateRequest;
import org.cotato.backend.recruit.admin.dto.response.recruitmentInformation.RecruitmentInformationResponse;
import org.cotato.backend.recruit.admin.error.AdminErrorCode;
import org.cotato.backend.recruit.admin.exception.AdminException;
import org.cotato.backend.recruit.admin.service.generation.GenerationAdminService;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.cotato.backend.recruit.domain.recruitmentInformation.repository.RecruitmentInformationRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentInformationAdminService {
	private final RecruitmentInformationRepository recruitmentInformationRepository;
	private final GenerationAdminService generationAdminService;
	private final RecruitmentInformationUpserterManager recruitmentInformationUpserterManager;

	public RecruitmentInformationResponse getRecruitmentInformation(Long generationId) {
		Generation generation = generationAdminService.getGenerationById(generationId);

		List<RecruitmentInformation> informations =
				recruitmentInformationRepository.findByGeneration(generation);

		return RecruitmentInformationResponse.of(informations);
	}

	public RecruitmentInformation getRecruitmentInformation(
			Generation generation, InformationType informationType) {
		return recruitmentInformationRepository
				.findByGenerationAndInformationType(generation, informationType)
				.orElseThrow(
						() -> new AdminException(AdminErrorCode.RECRUITMENT_INFORMATION_NOT_FOUND));
	}

	@Transactional
	@CacheEvict(value = "recruitmentSchedule", allEntries = true)
	public void updateRecruitmentInformation(RecruitmentInformationUpdateRequest request) {
		validate(request);

		Generation generation = generationAdminService.getGenerationById(request.generationId());

		recruitmentInformationUpserterManager.upsertDatetime(
				generation, InformationType.RECRUITMENT_START, request.recruitmentStart());
		recruitmentInformationUpserterManager.upsertDatetime(
				generation, InformationType.RECRUITMENT_END, request.recruitmentEnd());
		recruitmentInformationUpserterManager.upsertDateStartOfDay(
				generation, InformationType.DOCUMENT_ANNOUNCEMENT, request.documentAnnouncement());
		recruitmentInformationUpserterManager.upsertDateStartOfDay(
				generation, InformationType.INTERVIEW_START, request.interviewStart());
		recruitmentInformationUpserterManager.upsertDateStartOfDay(
				generation, InformationType.INTERVIEW_END, request.interviewEnd());
		recruitmentInformationUpserterManager.upsertDateStartOfDay(
				generation, InformationType.FINAL_ANNOUNCEMENT, request.finalAnnouncement());
		recruitmentInformationUpserterManager.upsertDateStartOfDay(
				generation, InformationType.OT, request.ot());
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
