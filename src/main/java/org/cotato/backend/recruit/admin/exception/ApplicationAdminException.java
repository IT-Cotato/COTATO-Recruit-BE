package org.cotato.backend.recruit.admin.exception;

import lombok.Getter;
import org.cotato.backend.recruit.admin.error.ApplicationAdminErrorCode;

@Getter
public class ApplicationAdminException extends RuntimeException {
	private final ApplicationAdminErrorCode errorCode;

	public ApplicationAdminException(ApplicationAdminErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public ApplicationAdminException(ApplicationAdminErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public ApplicationAdminException(ApplicationAdminErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}
}
