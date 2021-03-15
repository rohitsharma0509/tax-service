package com.scb.rider.tax.service;

import com.scb.rider.tax.model.dto.FinalPaymentReconciliationDetails;
import com.scb.rider.tax.model.dto.GLAccountDetailList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static com.scb.rider.tax.constants.ExcelHeaderValue.*;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class ExcelGenerator {

    private static final Integer ROUND_PLACES = 2;


    public static Sheet createSheetWith(Workbook workbook, String name) {
        Sheet sheet = workbook.createSheet(name);
        sheet.setDefaultColumnWidth(20);
        return sheet;
    }

    public static void createHeaderRow(Sheet sheet, List<String> headers, CellStyle style) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = header.createCell(i);
            cell.setCellStyle(style);
            cell.setCellValue(headers.get(i));
        }
    }

    public static void createHeaderRow(Sheet sheet, List<String> headers, int rownum, CellStyle style) {
        Row header = sheet.createRow(rownum);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = header.createCell(i);
            cell.setCellStyle(style);
            cell.setCellValue(headers.get(i));
        }
    }

    public static void insertEntityData(Sheet sheet, Workbook workbook, List<FinalPaymentReconciliationDetails> finalDetails,
                                        CellStyle headerstyle, Double totalSecurityDeducted) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        Double rhJobTotal = 0.0;
        Double jobPriceTotal = 0.0;
        Double mdrTotal = 0.0;
        Double vatTotal = 0.0;
        Double netPaymentTotal = 0.0;
        int rowCount = 1;
        LocalDate localDate = LocalDate.now();
        String date = localDate.getDayOfMonth() + "-" + localDate.getMonth().name() + "-" + localDate.getYear();

        for (FinalPaymentReconciliationDetails entity : finalDetails) {
            Row row = sheet.createRow(rowCount);
            //row.setHeightInPoints(sheet.getDefaultRowHeightInPoints());
            row.createCell(0).setCellValue(rowCount);

            row.createCell(1).setCellValue(date);
            row.createCell(2).setCellValue(entity.getRaJobNumber());
            row.createCell(3).setCellValue(entity.getRaOrderStatus());
            row.createCell(4).setCellValue(entity.getRaRiderName());
            if (entity.getRhJobAmount() != null) {
                rhJobTotal = rhJobTotal + entity.getRhJobAmount();
            }
            if (entity.getRaJobAmount() != null) {
                Double jobPrice = entity.getRaJobAmount() != null ? round(entity.getRaJobAmount()) : 0.0;
                row.createCell(5).setCellValue(jobPrice);
                Double mdrValue = entity.getMdrValue() != null ? entity.getMdrValue() : 0.0;
                Double vatValue = entity.getVatValue() != null ? entity.getVatValue() : 0.0;
                row.createCell(6).setCellValue(mdrValue);
                row.createCell(7).setCellValue(vatValue);
                Double netPaymentAmount = round(entity.getRaPaymentAmount());
                row.createCell(8).setCellValue(netPaymentAmount);
                jobPriceTotal = jobPriceTotal + jobPrice;
                mdrTotal = mdrTotal + mdrValue;
                vatTotal = vatTotal + vatValue;
                netPaymentTotal = netPaymentTotal + netPaymentAmount;
            } else {
                Double amount = round(entity.getRaPaymentAmount());
                row.createCell(5).setCellValue(amount);
                row.createCell(6).setCellValue(0.0);
                row.createCell(7).setCellValue(0.0);
                row.createCell(8).setCellValue(amount);
                jobPriceTotal = jobPriceTotal + amount;
                mdrTotal = mdrTotal + 0.0;
                vatTotal = vatTotal + 0.0;
                netPaymentTotal = netPaymentTotal + amount;
            }
            rowCount++;
        }
        Row row = sheet.createRow(rowCount);
        row.createCell(4).setCellValue("Total");
        row.createCell(5).setCellValue(jobPriceTotal);
        row.createCell(6).setCellValue(mdrTotal);
        row.createCell(7).setCellValue(vatTotal);
        row.createCell(8).setCellValue(netPaymentTotal);

        setRegionBorderWithMedium(CellRangeAddress.valueOf("A1:I" + rowCount), sheet);
        insertDataForGLDetails(rowCount, sheet, date, jobPriceTotal, mdrTotal, vatTotal, netPaymentTotal, headerstyle, totalSecurityDeducted, rhJobTotal);

    }

    private static void setRegionBorderWithMedium(CellRangeAddress region, Sheet sheet) {
        RegionUtil.setBorderBottom(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderRight(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderTop(BorderStyle.MEDIUM, region, sheet);

    }

    private static void insertDataForGLDetails(int rownum, Sheet sheet, String date, Double jobPriceTotal,
                                               Double mdrTotal, Double vatTotal, Double netPaymentTotal, CellStyle style,
                                               Double totalSecurityDeducted, Double rhJobTotal) {
        rownum = rownum + 5;
        createHeaderRow(sheet, getHeadersForConsolidated(), rownum, style);
        rownum++;
        int count = 1;
        Double subsidyAmount = round(rhJobTotal - jobPriceTotal);
        List<GLAccountDetailList> glDetails = getAccountDetailsList();

        for (GLAccountDetailList gl : glDetails) {
            Row row = sheet.createRow(rownum);
            //row.setHeightInPoints(sheet.getDefaultRowHeightInPoints());
            row.createCell(0).setCellValue(date);
            row.createCell(1).setCellValue(gl.getDocumentNumber());
            row.createCell(2).setCellValue(gl.getBusinessUnitCode());
            row.createCell(3).setCellValue(gl.getAccountType());
            row.createCell(4).setCellValue(gl.getAccountNumber());
            row.createCell(5).setCellValue(gl.getDescription());
            row.createCell(6).setCellValue(gl.getCurrencyCode());
            if (count == 1)
                row.createCell(7).setCellValue(rhJobTotal);
            if (count == 2)
                row.createCell(8).setCellValue(round(netPaymentTotal - totalSecurityDeducted));
            if (count == 3)
                row.createCell(8).setCellValue(mdrTotal);
            if (count == 4)
                row.createCell(8).setCellValue(vatTotal);
            if (count == 5)
                row.createCell(8).setCellValue(round(totalSecurityDeducted));
            if (count == 6)
                row.createCell(8).setCellValue(subsidyAmount);
            count++;
            rownum++;
        }
    }

    public static Double round(Double value) {
        if (value == null)
            return null;
        BigDecimal bd = new BigDecimal(String.valueOf(value));
        bd = bd.setScale(ROUND_PLACES, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Double round(String value) {
        if (value == null)
            return null;
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(ROUND_PLACES, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static void wrapText(Workbook workbook, Cell cell) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);
        cell.setCellStyle(cellStyle);
    }

    private static List<String> getHeadersForConsolidated() {
        return getStringInList(POSTING_DATE, DOCUMENT_NO, BUSINESS_UNIT, ACCOUNT_NUMBER, ACCOUNT_TYPE, DESCRIPTION, CURRENCY_CODE, DEBIT_AMOUNT, CREDIT_AMOUNT);
    }

    public static List<String> getStringInList(String... values) {
        return Arrays.asList(values);
    }

    private static List<GLAccountDetailList> getAccountDetailsList() {
        return Arrays.asList(new GLAccountDetailList("", "", "GL account", "2090-20029510", "Inter App - RBH Rider", "THB"),
                new GLAccountDetailList("", "", "GL account", "2090-20029512", "AP Rider individual", "THB"),
                new GLAccountDetailList("", "", "GL account", "4029-40044908", "GP Revenue (=MDR 1.85%)", "THB"),
                new GLAccountDetailList("", "", "GL account", "2090-20029414", "GP OutputVAT", "THB"),
                new GLAccountDetailList("", "", "GL account", "XXXX-XXXXXXXX", "Security Deposit", "THB"),
                new GLAccountDetailList("", "", "GL account", "XXXX-XXXXXXXX", "RBH Profit/Subsidy", "THB"));
    }
}
