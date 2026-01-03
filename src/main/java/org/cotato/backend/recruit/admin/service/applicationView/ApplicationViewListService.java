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
import org.cotato.backend.recruit.domain.application.enums.PassStatus;
import org.cotato.backend.recruit.domain.application.repository.ApplicationRepository;
import org.cotato.backend.recruit.domain.generation.entity.Generation;
import org.cotato.backend.recruit.domain.question.enums.PartType;
import org.cotato.backend.recruit.domain.recruitmentInformation.entity.RecruitmentInformation;
import org.cotato.backend.recruit.domain.recruitmentInformation.enums.InformationType;
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

	private void validateRequest(ApplicationListRequest request) {
		// 기수 입력은 필수
		if (request.generation() == null) {
			throw new IllegalArgumentException("기수는 필수 입력값입니다.");
		}

		// partViewType 입력 필수
		if (request.partViewType() == null) {
			throw new IllegalArgumentException("파트 타입은 필수 입력값입니다.");
		}

		// passViewStatus 입력 필수
		if (request.passViewStatus() == null) {
			throw new IllegalArgumentException("패스 상태는 필수 입력값입니다.");
		}

		// ALL이 아닐때만 유효성 검사
		if (request.partViewType() != null && !request.partViewType().equals("ALL")) {
			PartType.isValidPartType(request.partViewType());
		}

		// ALL이 아닐때만 유효성 검사
		if (request.passViewStatus() != null && !request.passViewStatus().equals("ALL")) {
			PassStatus.isValidPassStatus(request.passViewStatus());
		}
	}

	public AdminApplicationsResponse getApplications(
			ApplicationListRequest request, Pageable pageable) {
		validateRequest(request);

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
				org.springframework.data.domain.PageRequest.of(
						pageable.getPageNumber(), pageable.getPageSize(), newSort);

		org.springframework.data.domain.Page<Application> applicationsPage =
				applicationRepository.findWithFilters(
						request.generation(),
						request.partViewType(),
						request.passViewStatus(),
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

	// 검색, 필터링 결과 summary
	private ApplicationSummaryResponse getSummaryResponse(ApplicationListRequest request) {
		// 1. DB 조회 (결과 예시: [[ "BE", 10 ], [ "FE", 5 ]])
		List<Object[]> counts =
				applicationRepository.countByFilterGroupByPartType(
						request.generation(),
						request.partViewType(),
						request.passViewStatus(),
						request.searchKeyword());

		// 2. 초기값 0으로 설정
		long pmCount = 0;
		long designCount = 0;
		long frontendCount = 0;
		long backendCount = 0;
		long totalCount = 0;

		// 3. 리스트 순회하며 매핑
		for (Object[] row : counts) {
			// row[0]: 파트 타입 (String)
			String typeStr = (String) row[0];

			// row[1]: 개수 (안전하게 String 변환 후 Long 파싱)
			long count = Long.parseLong(String.valueOf(row[1]));

			// 전체 개수 누적
			totalCount += count;

			// 파트별 분기 처리
			switch (typeStr) {
				case "PM" -> pmCount = count;
				case "DE" -> designCount = count;
				case "FE" -> frontendCount = count;
				case "BE" -> backendCount = count;
			}
		}

		return ApplicationSummaryResponse.builder()
				.totalCount(totalCount)
				.pmCount(pmCount)
				.designCount(designCount)
				.frontendCount(frontendCount)
				.backendCount(backendCount)
				.build();
	}

	private ApplicationElementResponse toApplicationElementResponse(Application app) {
		return ApplicationElementResponse.builder()
				.applicationId(app.getId())
				.name(app.getName())
				.gender(app.getGender())
				.part(app.getPartType())
				.university(app.getUniversity())
				.phoneNumber(app.getPhoneNumber())
				.passStatus(app.getPassStatus())
				.build();
	}
}
