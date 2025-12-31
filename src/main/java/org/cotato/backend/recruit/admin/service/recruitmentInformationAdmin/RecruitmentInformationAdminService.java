package org.cotato.backend.recruit.admin.service.recruitmentInformationAdmin;

import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.cotato.backend.recruit.domain.recruitmentInformation.repository.RecruitmentInformationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecruitmentInformationAdminService {
	private final RecruitmentInformationRepository recruitmentInformationRepository;

	public RecruitmentInformation getRecruitmentInformation(
			Generation generation, InformationType informationType) {
		return recruitmentInformationRepository
				.findByGenerationAndInformationType(generation, informationType)
				.orElseThrow(() -> new RuntimeException("RecruitmentInformation not found"));
	}
}
