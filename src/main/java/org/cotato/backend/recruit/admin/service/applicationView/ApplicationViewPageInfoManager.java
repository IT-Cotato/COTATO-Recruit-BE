package org.cotato.backend.recruit.admin.service.applicationView;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationViewPageInfoManager {

	// private final ApplicationRepository applicationRepository;

	// public PageInfoResponse getApplicationPageInfo(
	// ApplicationListRequest request, Pageable pageable) {
	// long totalElements = countApplicationsWithFilter(request);
	// int pageSize = pageable.getPageSize();
	// int totalPages = calculateTotalPages(totalElements, pageSize);
	// int currentPage = pageable.getPageNumber() + 1;

	// return PageInfoResponse.builder()
	// .currentPage(currentPage)
	// .totalPages(totalPages == 0 ? 1 : totalPages)
	// .totalElements(totalElements)
	// .size(pageSize)
	// .build();
	// }

	// private long countApplicationsWithFilter(ApplicationListRequest request) {
	// return applicationRepository.countApplicationsWithFilter(
	// request.generation(),
	// request.partViewType().name(),
	// request.passViewStatus().name(),
	// request.searchKeyword());
	// }

	// private int calculateTotalPages(long totalElements, int pageSize) {
	// return (int) Math.ceil((double) totalElements / pageSize);
	// }
}
