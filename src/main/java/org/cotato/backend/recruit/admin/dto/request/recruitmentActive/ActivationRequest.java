package org.cotato.backend.recruit.admin.dto.request.recruitmentActive;

import java.time.LocalDate;

public record ActivationRequest(Long generation, LocalDate startDate, LocalDate endDate) {}
