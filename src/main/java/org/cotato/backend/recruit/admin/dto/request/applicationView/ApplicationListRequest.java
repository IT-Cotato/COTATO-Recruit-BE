package org.cotato.backend.recruit.admin.dto.request.applicationView;

public record ApplicationListRequest(
		Long generation, String searchKeyword, String partViewType, String passViewStatus) {}
