package org.cotato.backend.recruit.admin.service.applicationView;

import lombok.RequiredArgsConstructor;
import org.cotato.backend.recruit.admin.dto.response.applicationView.PageInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationViewPageInfoManager {

	public PageInfoResponse getPageInfo(Page<?> page) {
		int totalPages = page.getTotalPages();
		long totalElements = page.getTotalElements();
		int currentPage = page.getNumber() + 1;
		int size = page.getSize();

		return PageInfoResponse.builder()
				.currentPage(currentPage)
				.totalPages(totalPages == 0 ? 1 : totalPages)
				.totalElements(totalElements)
				.size(size)
				.build();
	}
}
