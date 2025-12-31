package org.cotato.backend.recruit.admin.dto.request.email;

import jakarta.validation.constraints.NotBlank;

public record EmailContentUpdateRequest(@NotBlank(message = "메일 내용은 필수입니다.") String content) {}
