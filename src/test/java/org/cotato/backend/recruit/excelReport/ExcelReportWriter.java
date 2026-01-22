package org.cotato.backend.recruit.excelReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReportWriter {

	private static final String FILE_NAME = "integration_test_report.xlsx";
	private static final String[] COLUMNS = {
		"Test Class Name", "API URL", "Test Name", "Status", "Message", "Details"
	};

	public File write(List<TestResultDto> results) {
		if (results.isEmpty()) return null;

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Integration Test Results");
			createHeader(workbook, sheet);
			createDataRows(workbook, sheet, results);
			autoSizeColumns(sheet);

			File file = new File(FILE_NAME);
			try (FileOutputStream fileOut = new FileOutputStream(file)) {
				workbook.write(fileOut);
				System.out.println("\n✅ [ExcelWriter] 엑셀 파일 생성 완료: " + file.getAbsolutePath());
				return file;
			}
		} catch (IOException e) {
			System.err.println("❌ [ExcelWriter] 파일 생성 중 오류 발생: " + e.getMessage());
			return null;
		}
	}

	private void createHeader(Workbook workbook, Sheet sheet) {
		Row headerRow = sheet.createRow(0);
		CellStyle headerStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		headerStyle.setFont(font);

		for (int i = 0; i < COLUMNS.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(COLUMNS[i]);
			cell.setCellStyle(headerStyle);
		}
	}

	private void createDataRows(Workbook workbook, Sheet sheet, List<TestResultDto> results) {
		int rowNum = 1;
		String previousApiUrl = null; // 이전 API URL을 저장할 변수

		// FAIL인 경우 사용할 빨간색 폰트 스타일 생성
		CellStyle failStyle = workbook.createCellStyle();
		Font redFont = workbook.createFont();
		redFont.setColor(IndexedColors.RED.getIndex());
		failStyle.setFont(redFont);

		synchronized (results) {
			// API URL 기준으로 정렬
			results.sort(Comparator.comparing(TestResultDto::getApiUrl));

			for (TestResultDto data : results) {
				String currentApiUrl = data.getApiUrl();

				// 이전 URL이 존재하고(첫 번째 행이 아니고), 현재 URL과 다르면
				if (previousApiUrl != null && !currentApiUrl.equals(previousApiUrl)) {
					rowNum += 2; // 2칸 띄우기 (빈 행 2개 추가)
				}

				Row row = sheet.createRow(rowNum++);
				boolean isFail = "FAIL".equals(data.getStatus());

				// 각 셀 생성 및 FAIL인 경우 빨간색 스타일 적용
				Cell cell0 = row.createCell(0);
				cell0.setCellValue(data.getTestClassName());
				if (isFail) cell0.setCellStyle(failStyle);

				Cell cell1 = row.createCell(1);
				cell1.setCellValue(data.getApiUrl());
				if (isFail) cell1.setCellStyle(failStyle);

				Cell cell2 = row.createCell(2);
				cell2.setCellValue(data.getTestName());
				if (isFail) cell2.setCellStyle(failStyle);

				Cell cell3 = row.createCell(3);
				cell3.setCellValue(data.getStatus());
				if (isFail) cell3.setCellStyle(failStyle);

				Cell cell4 = row.createCell(4);
				cell4.setCellValue(data.getMessage());
				if (isFail) cell4.setCellStyle(failStyle);

				String details = data.getDetails();
				if (details != null && details.length() > 5000) {
					details = details.substring(0, 5000) + "... (truncated)";
				}
				Cell cell5 = row.createCell(5);
				cell5.setCellValue(details);
				if (isFail) cell5.setCellStyle(failStyle);

				// 현재 URL을 이전 URL 변수에 저장
				previousApiUrl = currentApiUrl;
			}
		}
	}

	private void autoSizeColumns(Sheet sheet) {
		for (int i = 0; i < COLUMNS.length; i++) {
			sheet.autoSizeColumn(i);
		}
	}
}
