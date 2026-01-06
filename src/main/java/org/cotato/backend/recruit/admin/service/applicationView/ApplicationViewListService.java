package org.cotato.backend.recruit.admin.service.applicationView;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.applicationView.ApplicationListRequest;
import org.cotato.backend.recruit.admin.dto.response.applicationView.AdminApplicationsResponse;
import org.cotato.backend.recruit.admin.dto.response.applicationView.AdminApplicationsResponse.Applicants;
import org.cotato.backend.recruit.admin.dto.response.applicationView.ApplicationElementResponse;
import org.cotato.backend.recruit.admin.dto.response.applicationView.ApplicationSummaryResponse;
import org.cotato.backend.recruit.admin.dto.response.applicationView.PageInfoResponse;
import org.cotato.backend.recruit.admin.dto.response.recruitmentInformation.RecruitmentPeriodResponse;
import org.cotato.backend.recruit.admin.service.generation.GenerationAdminService;
import org.cotato.backend.recruit.admin.service.recruitmentInformation.RecruitmentInformationAdminService;
import org.cotato.backend.recruit.domain.application.entity.Application;
import org.cotato.backend.recruit.domain.application.repository.ApplicationRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

	public AdminApplicationsResponse getApplications(
			ApplicationListRequest request, Pageable pageable) {

		// 정렬 로직 처리
		// 1. 기본값: 지원제출최신순(submitted_at DESC) -> 이름 오름차순 고정
		// 2. 이름 정렬시: 지원제출최신순 풀림
		Sort sort = pageable.getSort();
		Sort.Order nameOrder = sort.getOrderFor("name");

		Sort newSort;
		if (nameOrder == null) {
			// 이름 정렬이 없으면 (기본값 or submittedAt) -> submitted_at DESC, name ASC 고정
			// Native Query이므로 DB 컬럼명 사용 (submitted_at)
			newSort =
					Sort.by(Sort.Direction.DESC, "submitted_at")
							.and(Sort.by(Sort.Direction.ASC, "name"));
		} else {
			// 이름 정렬이 있으면 해당 정렬 유지.
			// Pageable의 name property는 DB 컬럼 name과 동일하므로 그대로 사용 가능.
			newSort = sort;
		}

		Pageable newPageable =
				PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), newSort);

		Page<Application> applicationsPage =
				applicationRepository.findWithFilters(
						request.generation(),
						request.partViewType().name(),
						request.passViewStatus().name(),
						request.searchKeyword(),
						newPageable);

		List<ApplicationElementResponse> content =
				applicationsPage.getContent().stream()
						.map(this::toApplicationElementResponse)
						.toList();

		Generation generation = generationAdminService.getGenerationById(request.generation());
		RecruitmentPeriodResponse recruitmentPeriodResponse =
				getRecruitmentPeriodResponse(generation);

		// 파트별 지원자수 통계
		ApplicationSummaryResponse summary = getSummaryResponse(request);

		// 페이징 정보
		PageInfoResponse pageInfo = applicationViewPageInfoManager.getPageInfo(applicationsPage);

		return AdminApplicationsResponse.builder()
				.recruitmentPeriodResponse(recruitmentPeriodResponse)
				.summary(summary)
				.applicants(Applicants.builder().content(content).pageInfo(pageInfo).build())
				.build();
	}

	private RecruitmentPeriodResponse getRecruitmentPeriodResponse(Generation generation) {
		RecruitmentInformation recruitmentStart =
				recruitmentInformationAdminService.getRecruitmentInformation(
						generation, InformationType.RECRUITMENT_START);
		RecruitmentInformation recruitmentEnd =
				recruitmentInformationAdminService.getRecruitmentInformation(
						generation, InformationType.RECRUITMENT_END);

		return RecruitmentPeriodResponse.builder()
				.recruitmentStart(recruitmentStart.getEventDatetime())
				.recruitmentEnd(recruitmentEnd.getEventDatetime())
				.build();
	}

	private ApplicationSummaryResponse getSummaryResponse(ApplicationListRequest request) {
		List<Object[]> counts =
				applicationRepository.countByFilterGroupByPartType(
						request.generation(),
						request.partViewType().name(),
						request.passViewStatus().name(),
						request.searchKeyword());

		return ApplicationSummaryResponse.from(counts);
	}

	private ApplicationElementResponse toApplicationElementResponse(Application application) {
		return ApplicationElementResponse.from(application);
	}
}
