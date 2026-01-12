package org.cotato.backend.recruit.admin.dto.request.applicationView;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.cotato.backend.recruit.admin.enums.PartViewType;
import org.cotato.backend.recruit.admin.enums.PassViewStatus;

public record ApplicationListRequest(
		@NotNull Long generationId,
		String searchKeyword,
		@NotNull PartViewType partViewType,
		@NotNull List<PassViewStatus> passViewStatuses) {}
