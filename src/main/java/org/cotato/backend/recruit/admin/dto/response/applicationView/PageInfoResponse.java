package org.cotato.backend.recruit.admin.dto.response.applicationView;

import lombok.Builder;

@Builder
public record PageInfoResponse(int currentPage, int totalPages, long totalElements, int size) {}
