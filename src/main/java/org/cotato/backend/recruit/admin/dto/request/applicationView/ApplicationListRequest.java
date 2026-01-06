package org.cotato.backend.recruit.admin.dto.request.applicationView;

import jakarta.validation.constraints.NotNull;
import org.cotato.backend.recruit.admin.enums.PartViewType;
import org.cotato.backend.recruit.admin.enums.PassViewStatus;

public record ApplicationListRequest(
		@NotNull Long generation,
		String searchKeyword,
		@NotNull PartViewType partViewType,
		@NotNull PassViewStatus passViewStatus) {}
