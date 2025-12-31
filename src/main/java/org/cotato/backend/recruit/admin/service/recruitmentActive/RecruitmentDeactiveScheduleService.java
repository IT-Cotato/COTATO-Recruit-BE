package org.cotato.backend.recruit.admin.service.recruitmentActive;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.cotato.backend.recruit.domain.recruitmentInformation.repository.RecruitmentInformationRepository;
import org.cotato.backend.recruit.presentation.service.GenerationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecruitmentDeactiveScheduleService {
	private final GenerationService generationService;
	private final RecruitmentInformationRepository recruitmentInformationRepository;

	// 스케줄러로 모집 비활성화
	// 매일 자정에 recruitment_end 정보를 확인하여 모집 비활성화
	@Scheduled(cron = "0 0 0 * * *")
	public void deactivateRecruitment() {
		// 활성화 상태의 기수 찾기
		Generation generation = generationService.getActiveGeneration();

		// 해당 기수의 recruitment_end 정보가 현재 시간보다 이전이면 모집 비활성화
		if (isRecruitmentEndBeforeNow(generation)) {
			generation.updateRecruitmentStatus(false);
		}
	}

	private boolean isRecruitmentEndBeforeNow(Generation generation) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime recruitmentEnd =
				recruitmentInformationRepository
						.findByGenerationAndInformationType(
								generation, InformationType.RECRUITMENT_END)
						.map(RecruitmentInformation::getEventDatetime)
						.orElse(null);
		return recruitmentEnd != null && recruitmentEnd.isBefore(now);
	}
}
