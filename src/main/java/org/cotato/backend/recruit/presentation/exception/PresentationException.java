package org.cotato.backend.recruit.presentation.exception;

import lombok.Getter;
import org.cotato.backend.recruit.presentation.error.PresentationErrorCode;

@Getter
public class PresentationException extends RuntimeException {

	private final PresentationErrorCode errorCode;

	public PresentationException(PresentationErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public PresentationException(PresentationErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public PresentationException(PresentationErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}
}
