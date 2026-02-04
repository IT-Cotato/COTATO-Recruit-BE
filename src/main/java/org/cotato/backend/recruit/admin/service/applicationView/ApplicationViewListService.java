package org.cotato.backend.recruit.admin.service.applicationView;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.request.applicationView.ApplicationListRequest;
import org.cotato.backend.recruit.admin.dto.response.applicationView.AdminApplicationsResponse;
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
	private static final String SUBMITTED_AT_FIELD = "submittedAt"; // Pageable 요청용
	private static final String SUBMITTED_AT_COLUMN = "submitted_at"; // 네이티브 쿼리용
	private static final String NAME_COLUMN = "name"; // 네이티브 쿼리용

	private final ApplicationRepository applicationRepository;
	private final RecruitmentInformationAdminService recruitmentInformationAdminService;
	private final ApplicationViewPageInfoManager applicationViewPageInfoManager;
	private final GenerationAdminService generationAdminService;

	public AdminApplicationsResponse getApplications(
			ApplicationListRequest request, Pageable pageable) {

		// 정렬 로직 처리
		// 정렬 기준: 지원제출최신순(submitted_at) -> 이름 오름차순 고정
		Pageable sortedPageable = createPageableWithCustomSort(pageable);

		Page<Application> applicationsPage =
				applicationRepository.findWithFilters(
						request.generationId(),
						request.partViewType().name(),
						request.passViewStatuses().stream().map(Enum::name).toList(),
						request.searchKeyword(),
						sortedPageable);

		List<ApplicationElementResponse> content =
				applicationsPage.getContent().stream()
						.map(ApplicationElementResponse::from)
						.toList();

		// 기수 부가 정보
		Generation generation = generationAdminService.getGenerationById(request.generationId());
		RecruitmentPeriodResponse recruitmentPeriodResponse =
				getRecruitmentPeriodResponse(generation);

		// 파트별 지원자수 통계
		ApplicationSummaryResponse summary = getSummaryResponse(request);

		// 페이징 정보
		PageInfoResponse pageInfo = applicationViewPageInfoManager.getPageInfo(applicationsPage);

		return AdminApplicationsResponse.of(recruitmentPeriodResponse, summary, content, pageInfo);
	}

	private Pageable createPageableWithCustomSort(Pageable pageable) {
		Sort.Order submittedAtOrder = pageable.getSort().getOrderFor(SUBMITTED_AT_FIELD);

		// 기본값 DESC 설정
		Sort.Direction direction =
				(submittedAtOrder != null) ? submittedAtOrder.getDirection() : Sort.Direction.DESC;

		// 정렬 우선순위: 제출일시 -> 이름 (고정)
		Sort newSort =
				Sort.by(direction, SUBMITTED_AT_COLUMN)
						.and(Sort.by(Sort.Direction.ASC, NAME_COLUMN));

		return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), newSort);
	}

	private RecruitmentPeriodResponse getRecruitmentPeriodResponse(Generation generation) {
		RecruitmentInformation recruitmentStart =
				recruitmentInformationAdminService.getRecruitmentInformation(
						generation, InformationType.RECRUITMENT_START);
		RecruitmentInformation recruitmentEnd =
				recruitmentInformationAdminService.getRecruitmentInformation(
						generation, InformationType.RECRUITMENT_END);

		return RecruitmentPeriodResponse.of(
				recruitmentStart.getEventDatetime(), recruitmentEnd.getEventDatetime());
	}

	private ApplicationSummaryResponse getSummaryResponse(ApplicationListRequest request) {
		List<Object[]> counts =
				applicationRepository.countByFilterGroupByApplicationPartType(
						request.generationId(),
						request.passViewStatuses().stream().map(Enum::name).toList(),
						request.searchKeyword());

		return ApplicationSummaryResponse.from(counts);
	}
}
