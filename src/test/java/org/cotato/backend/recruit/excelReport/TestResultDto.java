package org.cotato.backend.recruit.excelReport;

import lombok.Getter;

@Getter
public class TestResultDto {
	private String apiUrl;
	private String testName;
	private String status;
	private String message;

	public TestResultDto(String apiUrl, String testName, String status, String message) {
		this.apiUrl = apiUrl;
		this.testName = testName;
		this.status = status;
		this.message = message;
	}
}
