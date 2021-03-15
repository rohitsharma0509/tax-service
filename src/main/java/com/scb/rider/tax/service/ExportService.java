package com.scb.rider.tax.service;

import com.scb.rider.tax.exception.ExcelFileGenerationException;
import com.scb.rider.tax.model.dto.FinalPaymentReconciliationDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import static com.scb.rider.tax.constants.ExcelHeaderValue.*;
import static com.scb.rider.tax.service.ExcelGenerator.createHeaderRow;
import static com.scb.rider.tax.service.ExcelGenerator.createSheetWith;


@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class ExportService {

    public byte[] buildEntityExcelDocument(List<FinalPaymentReconciliationDetails> data, Double totalSecurityDeducted) throws Exception {

        log.info("Generating file for GL");
        try (Workbook workbook = new XSSFWorkbook();) {
            Sheet sheet = createSheetWith(workbook, "GL Report");
            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            style.setFont(font);

            createHeaderRow(sheet, getHeadersForJob(), style);
            ExcelGenerator.insertEntityData(sheet, workbook, data, style, totalSecurityDeducted);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                workbook.write(bos);
            } finally {
                bos.close();
            }
            byte[] bytes = bos.toByteArray();
            return bytes;

        } catch (Exception ex) {
            log.info("Exception while Generation of file or sending email ");
            throw new ExcelFileGenerationException("Exception while Generation of file or sending email");
        }
    }

    private static List<String> getHeadersForJob() {
        return getStringInList(NO, DATE, JOB_NO, ORDER_STATUS, RIDER_NAME, JOB_PRICE, MDR_VALUE, VAT_VALUE, NET_AMOUNT_PAID);
    }

    public static List<String> getStringInList(String... values) {
        return Arrays.asList(values);
    }
}
