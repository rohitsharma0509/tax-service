package com.scb.rider.tax.model.dto;

import static com.scb.rider.tax.constants.ExcelHeaderValue.*;
import static com.scb.rider.tax.constants.ExcelStaticValues.*;
import static org.apache.poi.hssf.record.cf.BorderFormatting.BORDER_THIN;
import static org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;
import com.scb.rider.tax.constants.Constants;
import com.scb.rider.tax.util.CommonUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExcelGLJobDetails {

	private static final DecimalFormat df = new DecimalFormat("#.00");

	private static final Map<String, String> jobTypeMap = ImmutableMap.of(
			"1", ORDER_TYPE_EXPRESS,
			"2", ORDER_TYPE_MART,
			"3", ORDER_TYPE_FOOD
	);

	public static byte[] generateGlReport(List<FinalPaymentReconciliationDetails> listOfReconDetails) throws IOException {
		log.info("generating GL report");
		try (Workbook workbook = new SXSSFWorkbook()) {
			CellStyle headerStyle = getHeaderStyle(workbook);
			CellStyle normalStyle = getNormalStyle(workbook);
			Sheet sheet = workbook.createSheet("GL Report");
			int rowCount = 0;
			createRow(sheet, rowCount, getGlReportHeaders(), headerStyle);
			rowCount++;

			String currentDate = CommonUtils.getFormattedCurrentDate(Constants.DATE_FORMAT_DDMMMYY);
			for(FinalPaymentReconciliationDetails reconDetails : listOfReconDetails) {
				log.debug("rowCount: {}", rowCount);
				Double otherDeductions = Objects.nonNull(reconDetails.getOtherDeductions()) ? reconDetails.getOtherDeductions(): 0;
				Double paymentAmount = CommonUtils.round(reconDetails.getRaPaymentAmount() - otherDeductions);
				String batchName = getBatchName(reconDetails.getRaJobType());
				String jobType = jobTypeMap.get(reconDetails.getRaJobType());
				List<String> values = Arrays.asList(batchName,
						currentDate,
						StringUtils.EMPTY,
						reconDetails.getRhOrderNumber(),
						EVENT_CODE,
						jobType,
						COMPANY_CODE,
						RC_CODE,
						OC_CODE,
						CHANNEL_CODE,
						PRODUCT_CODE,
						INTER_APP_NAV_ACCOUNT,
						SCB_ACCOUNT_CODE_1,
						PROJECT_CODE,
						TAX_CODE,
						INTER_CO_CODE,
						FUTURE_1_CODE,
						FUTURE_2_CODE,
						CURRENCY_CODE,
						df.format(paymentAmount),
						StringUtils.EMPTY,
						ACC_DESC_INTER_APP);
				createRow(sheet, rowCount, values, normalStyle);
				rowCount++;
				values = Arrays.asList(
						batchName,
						currentDate,
						StringUtils.EMPTY,
						reconDetails.getRhOrderNumber(),
						EVENT_CODE,
						jobType,
						COMPANY_CODE,
						RC_CODE,
						OC_CODE,
						CHANNEL_CODE,
						PRODUCT_CODE,
						NAV_ACCOUNT_CODE.concat(SCB_ACCOUNT_CODE_2),
						SCB_ACCOUNT_CODE_2,
						PROJECT_CODE,
						TAX_CODE,
						INTER_CO_CODE,
						FUTURE_1_CODE,
						FUTURE_2_CODE,
						CURRENCY_CODE,
						StringUtils.EMPTY,
						df.format(paymentAmount),
						ACC_DESC_SUSPENSE
				);
				createRow(sheet, rowCount, values, normalStyle);
				rowCount++;
			}
			ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
			workbook.write(outByteStream);
			outByteStream.flush();
			outByteStream.close();
			log.info("byte array size: {}", outByteStream.size());
			return outByteStream.toByteArray();
		}
	}

	public static void createRow(Sheet sheet, int rowCount, List<String> values, CellStyle cellStyle) {
		Row row = sheet.createRow(rowCount);
		for(int cellNum = 0 ; cellNum < values.size() ; cellNum++) {
			Cell cell = row.createCell(cellNum);
			cell.setCellValue(values.get(cellNum));
			cell.setCellStyle(cellStyle);
		}
	}

	private static List<String> getGlReportHeaders() {
		return Arrays.asList(BATCH_NAME, BATCH_DATE, ACCOUNTING_DATE, ORDER_ID, EVENT, ORDER_TYPE, COMPANY, RC, OC,
				CHANNEL, PRODUCT, NAV_ACCOUNT, SCB_ACCOUNT, PROJECT, TAX, INTER_CO, FUTURE_1, FUTURE_2, CURRENCY, ENTERED_DEBIT, ENTERED_CREDIT, ACCOUNT_DESCRIPTION);
	}

	protected static CellStyle getHeaderStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setFillPattern(SOLID_FOREGROUND);
		style.setBorderBottom(BorderStyle.valueOf(BORDER_THIN));
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(BorderStyle.valueOf(BORDER_THIN));
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderRight(BorderStyle.valueOf(BORDER_THIN));
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(BorderStyle.valueOf(BORDER_THIN));
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		return style;
	}


	protected static CellStyle getNormalStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setBorderBottom(BorderStyle.valueOf(BORDER_THIN));
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(BorderStyle.valueOf(BORDER_THIN));
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderRight(BorderStyle.valueOf(BORDER_THIN));
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(BorderStyle.valueOf(BORDER_THIN));
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		return style;
	}

	private static String getBatchName(String jobType) {
		StringBuilder sb = new StringBuilder(BATCH_NAME_PLACEHOLDER);
		sb.append(Constants.UNDERSCORE).append(jobTypeMap.get(jobType)).append(Constants.UNDERSCORE);
		sb.append(CommonUtils.getFormattedCurrentDate(Constants.FORMAT_YYYYMMDD));
		return sb.toString();
	}

}
