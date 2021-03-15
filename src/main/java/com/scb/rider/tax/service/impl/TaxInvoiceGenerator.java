package com.scb.rider.tax.service.impl;

import com.opencsv.CSVWriter;
import com.scb.rider.tax.client.impl.RiderServiceClient;
import com.scb.rider.tax.constants.Constants;
import com.scb.rider.tax.model.dto.BatchConfigurationDto;
import com.scb.rider.tax.model.dto.RiderProfileDto;
import com.scb.rider.tax.model.dto.RiderTaxInvoiceDetails;
import com.scb.rider.tax.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaxInvoiceGenerator {

    private static final String CHK_FILE_SEPARATOR = "\\|";
    private static final DecimalFormat df = new DecimalFormat("#.00");

    @Value("${tax-invoice.csv-file.invoice-number-format}")
    private String invoiceNumberFormat;

    @Autowired
    private RiderServiceClient riderServiceClient;

    public ByteArrayOutputStream toCsvFile(LocalDateTime paymentDateTime, BatchConfigurationDto config, Map<String, RiderTaxInvoiceDetails> riderTaxInvoiceDetailsMap) throws IOException {
        Map<String, RiderProfileDto> riderProfileMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(riderTaxInvoiceDetailsMap)) {
            List<String> riderIds = new ArrayList<>(riderTaxInvoiceDetailsMap.keySet());
            List<RiderProfileDto> riderProfiles = riderServiceClient.getRiderProfilesByRiderIds(riderIds);
            riderProfileMap = riderProfiles.stream().collect(Collectors.toMap(RiderProfileDto::getRiderId, profile -> profile));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)
        ) {
            String[] headers = {"invoice_number", "branch_code", "buyer_type", "buyer_tax_id", "buyer_name",
                    "buyer_email", "buyer_email_cc", "buyer_zipcode", "buyer_country", "buyer_address_1",
                    "buyer_address_2", "currency", "amount", "tax_code_type", "tax_rate", "tax_amount",
                    "fee_amount", "discount_amount", "total_amount_before_tax", "total_amount_tax", "total_amount",
                    "payment_condition", "due_date", "save_date", "document_reference", "document_reference_date",
                    "document_reference_code", "document_reason", "document_reason_code", "note", "export_date",
                    "pdf_password", "product_description", "product_quantity", "product_unit_code", "product_amount",
                    "product_amount_vat", "product_amount_per_txn", "product_total_amount_per_txn",
                    "product_additional1", "product_additional2", "product_additional3", "additional_01",
                    "additional_02", "additional_03", "additional_04", "additional_05", "additional_06",
                    "additional_07", "job_code", "upload_type", "pdf_template", "email_template"};
            csvWriter.writeNext(headers);

            if(!CollectionUtils.isEmpty(riderTaxInvoiceDetailsMap)) {
                int seq = 1;
                String currentDateString = CommonUtils.getFormattedCurrentDate(Constants.DATE_FORMAT_YYMMDD);

                for (RiderTaxInvoiceDetails riderTaxDetails : riderTaxInvoiceDetailsMap.values()) {
                    RiderProfileDto riderProfile = riderProfileMap.get(riderTaxDetails.getRiderId());
                    String zipcode = Objects.isNull(riderProfile.getAddress()) ? StringUtils.EMPTY : riderProfile.getAddress().getZipCode();
                    String address = Objects.isNull(riderProfile.getAddress()) ? StringUtils.EMPTY : riderProfile.getAddress().toString();
                    Double totalAmount = riderTaxDetails.getTotalMdrValue() + riderTaxDetails.getTotalVatValue();
                    String[] values = {
                            MessageFormat.format(invoiceNumberFormat, currentDateString, seq),
                            Constants.BRANCH_CODE, Constants.BUYER_TYPE,
                            riderProfile.getNationalID(), riderTaxDetails.getRiderName(),
                            StringUtils.EMPTY, StringUtils.EMPTY,
                            zipcode, Constants.BUYER_COUNTRY, address,
                            StringUtils.EMPTY, Constants.CURRENCY,
                            df.format(riderTaxDetails.getTotalMdrValue()),
                            Constants.TAX_CODE_TYPE,
                            config.getVatPercentage(),
                            df.format(riderTaxDetails.getTotalVatValue()),
                            StringUtils.EMPTY, StringUtils.EMPTY,
                            df.format(riderTaxDetails.getTotalMdrValue()),
                            df.format(riderTaxDetails.getTotalVatValue()),
                            df.format(totalAmount),
                            StringUtils.EMPTY, StringUtils.EMPTY,
                            CommonUtils.getFormattedDateTime(paymentDateTime, Constants.DATE_FORMAT_YYYYMMDD),
                            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
                            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
                            CommonUtils.getFormattedDateTime(paymentDateTime, Constants.DATE_FORMAT_YYYYMMDD),
                            StringUtils.EMPTY,
                            Constants.PRODUCT_DESCRIPTION,
                            Constants.PRODUCT_QUANTITY,
                            Constants.PRODUCT_UNIT_CODE,
                            df.format(riderTaxDetails.getTotalMdrValue()),
                            df.format(riderTaxDetails.getTotalVatValue()),
                            df.format(riderTaxDetails.getTotalMdrValue()),
                            df.format(totalAmount),
                            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
                            riderTaxDetails.getRiderAccountNumber(),
                            riderTaxDetails.getRiderName(),
                            riderTaxDetails.getRiderId(),
                            df.format(riderTaxDetails.getTotalPaymentAmount()),
                            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
                            Constants.JOB_CODE,
                            Constants.UPLOAD_TYPE,
                            Constants.PDF_TEMPLATE,
                            Constants.EMAIL_TEMPLATE
                    };
                    csvWriter.writeNext(values);
                    seq++;
                }
            }
        }
        return outputStream;
    }

    public ByteArrayOutputStream toCheckFile(String invoiceFileName, int noOfRecords, int size) throws IOException {
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        try (
            Writer w = new OutputStreamWriter(fos, StandardCharsets.UTF_8)
        ) {
            StringBuilder content = new StringBuilder(invoiceFileName);
            content.append(CHK_FILE_SEPARATOR).append(noOfRecords);
            content.append(CHK_FILE_SEPARATOR).append(size);

            String hash = CommonUtils.toHash(content.toString());
            w.append("file_name,total_record,size,hash_data").append(CSVWriter.RFC4180_LINE_END);
            w.append(content.toString()).append(CHK_FILE_SEPARATOR).append(hash).append(CSVWriter.RFC4180_LINE_END);
        }
        return fos;
    }
}
