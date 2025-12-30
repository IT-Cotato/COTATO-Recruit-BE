package org.cotato.backend.recruit.admin.exception;

import lombok.Getter;
import org.cotato.backend.recruit.admin.error.EmailAdminErrorCode;

@Getter
public class EmailAdminException extends RuntimeException {

	private final EmailAdminErrorCode errorCode;

	public EmailAdminException(EmailAdminErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public EmailAdminException(EmailAdminErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public EmailAdminException(EmailAdminErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}
}
