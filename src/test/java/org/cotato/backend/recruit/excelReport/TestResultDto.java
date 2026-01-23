package org.cotato.backend.recruit.excelReport;

import lombok.Getter;

@Getter
public class TestResultDto {
	private String testClassName;
	private String apiUrl;
	private String testName;
	private String status;
	private String message;
	private String details;

	public TestResultDto(
			String testClassName,
			String apiUrl,
			String testName,
			String status,
			String message,
			String details) {
		this.testClassName = testClassName;
		this.apiUrl = apiUrl;
		this.testName = testName;
		this.status = status;
		this.message = message;
		this.details = details;
	}
}
