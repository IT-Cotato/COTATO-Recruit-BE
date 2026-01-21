package org.cotato.backend.recruit.excelReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReportWriter {

	private static final String FILE_NAME = "integration_test_report.xlsx";
	private static final String[] COLUMNS = {"API URL", "Test Name", "Status", "Message"};

	public File write(List<TestResultDto> results) {
		if (results.isEmpty()) return null;

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Integration Test Results");
			createHeader(workbook, sheet);
			createDataRows(sheet, results);
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

	private void createDataRows(Sheet sheet, List<TestResultDto> results) {
		int rowNum = 1;
		synchronized (results) {
			for (TestResultDto data : results) {
				Row row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue(data.getApiUrl());
				row.createCell(1).setCellValue(data.getTestName());
				row.createCell(2).setCellValue(data.getStatus());
				row.createCell(3).setCellValue(data.getMessage());
			}
		}
	}

	private void autoSizeColumns(Sheet sheet) {
		for (int i = 0; i < COLUMNS.length; i++) {
			sheet.autoSizeColumn(i);
		}
	}
}
