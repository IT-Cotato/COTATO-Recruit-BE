package org.cotato.backend.recruit.admin.service.applicationView;

import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.response.applicationView.RecruitmentInformationResponse;
import org.cotato.backend.recruit.admin.service.generationAdmin.GenerationAdminService;
import org.cotato.backend.recruit.admin.service.recruitmentInformationAdmin.RecruitmentInformationAdminService;
import org.cotato.backend.recruit.domain.application.repository.ApplicationRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApplicationViewListService {

	private final ApplicationRepository applicationRepository;
	private final RecruitmentInformationAdminService recruitmentInformationAdminService;
	private final ApplicationViewPageInfoManager applicationViewPageInfoManager;
	private final GenerationAdminService generationAdminService;

	// public AdminApplicationsResponse getApplications(
	// ApplicationListRequest request, Pageable pageable) {

	// // 지원자 적은 학교순이 기본값
	// String universityDir = "DESC";

	// for (Sort.Order oder : pageable.getSort()) {
	// if (oder.getProperty().equals("university")) {
	// universityDir = oder.getDirection().name();
	// }
	// }

	// List<Application> applications =
	// applicationRepository.findApplicationsWithUniversitySort(
	// request.generation(),
	// request.partViewType().name(),
	// request.passViewStatus().name(),
	// request.searchKeyword(),
	// universityDir,
	// pageable.getPageSize(),
	// pageable.getOffset());

	// List<ApplicationElementResponse> content =
	// applications.stream().map(ApplicationElementResponse::from).toList();

	// Generation generation =
	// generationAdminService.findGeneration(request.generation());
	// RecruitmentInformationResponse recruitmentInformationResponse =
	// getRecruitmentInformationResponse(generation);

	// // 파트별 지원자수 통계
	// ApplicationSummaryResponse summary = getSummaryResponse(request);

	// // 필터링된 지원서 총 개수 - 페이징 계산용
	// // 필터링된 지원서 총 개수 및 페이징 정보 조회
	// PageInfoResponse pageInfo =
	// applicationViewPageInfoManager.getApplicationPageInfo(request, pageable);

	// return AdminApplicationsResponse.of(
	// recruitmentInformationResponse, summary, Applicants.of(content, pageInfo));
	// }

	private RecruitmentInformationResponse getRecruitmentInformationResponse(
			Generation generation) {
		RecruitmentInformation recruitmentStart =
				recruitmentInformationAdminService.getRecruitmentInformation(
						generation, InformationType.RECRUITMENT_START);
		RecruitmentInformation recruitmentEnd =
				recruitmentInformationAdminService.getRecruitmentInformation(
						generation, InformationType.RECRUITMENT_END);

		return RecruitmentInformationResponse.of(
				recruitmentStart.getEventDatetime(), recruitmentEnd.getEventDatetime());
	}

	// private ApplicationSummaryResponse getSummaryResponse(ApplicationListRequest
	// request) {
	// List<Object[]> counts = applicationRepository.countByFilterGroupByPartType(
	// request.generation(),
	// request.partViewType().name(),
	// request.passViewStatus().name(),
	// request.searchKeyword());

	// return ApplicationSummaryResponse.from(counts);
	// }
}
