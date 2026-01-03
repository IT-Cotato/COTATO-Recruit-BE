package org.cotato.backend.recruit.admin.exception;

import lombok.Getter;
import org.cotato.backend.recruit.admin.error.AdminErrorCode;

@Getter
public class AdminException extends RuntimeException {

	private final AdminErrorCode errorCode;

	public AdminException(AdminErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public AdminException(AdminErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public AdminException(AdminErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}
}
