package org.cotato.backend.recruit.admin.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApplicationAdminErrorCode {
    GENERATION_NOT_FOUND(HttpStatus.NOT_FOUND, "GEN-001", "해당 기수를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
