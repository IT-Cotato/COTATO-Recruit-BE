package org.cotato.backend.recruit.presentation.exception;

import lombok.Getter;
import org.cotato.backend.recruit.presentation.error.ApplicationErrorCode;

@Getter
public class ApplicationException extends RuntimeException {

	private final ApplicationErrorCode errorCode;

	public ApplicationException(ApplicationErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public ApplicationException(ApplicationErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public ApplicationException(ApplicationErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}
}
