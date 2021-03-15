package com.scb.rider.tax.service.impl;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.scb.rider.tax.client.impl.OperationServiceClient;
import com.scb.rider.tax.client.impl.ReconciliationServiceClient;
import com.scb.rider.tax.client.impl.SftpClient;
import com.scb.rider.tax.constants.Constants;
import com.scb.rider.tax.constants.ErrorConstants;
import com.scb.rider.tax.constants.SearchConstants;
import com.scb.rider.tax.exception.DataNotFoundException;
import com.scb.rider.tax.exception.ExternalServiceInvocationException;
import com.scb.rider.tax.exception.MatchedDataNotFoundException;
import com.scb.rider.tax.model.document.TaxInvoice;
import com.scb.rider.tax.model.dto.BatchConfigurationDto;
import com.scb.rider.tax.model.dto.RiderTaxInvoiceDetails;
import com.scb.rider.tax.model.dto.SearchResponseDto;
import com.scb.rider.tax.model.dto.TaxInvoiceDto;
import com.scb.rider.tax.model.enums.FileType;
import com.scb.rider.tax.model.enums.TaxInvoiceStatus;
import com.scb.rider.tax.repository.TaxInvoiceRepository;
import com.scb.rider.tax.service.TaxInvoiceService;
import com.scb.rider.tax.util.CommonUtils;
import com.scb.rider.tax.util.PropertyUtils;
import com.scb.rider.tax.util.SearchUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.skip;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

@Slf4j
@Service
public class TaxInvoiceServiceImpl implements TaxInvoiceService {

    @Value("${tax-invoice.csv-file.name-format}")
    private String invoiceFileNameFormat;

    @Autowired
    private TaxInvoiceRepository taxInvoiceRepository;

    @Autowired
    private ReconciliationServiceClient reconciliationServiceClient;

    @Autowired
    private OperationServiceClient operationServiceClient;

    @Autowired
    private TaxInvoiceGenerator taxInvoiceGenerator;

    @Autowired
    private SftpClient sftpClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PropertyUtils propertyUtils;

    @Override
    public SearchResponseDto searchTaxInvoices(List<String> filterQuery, Pageable pageable) {
        List<TaxInvoiceDto> invoices = new ArrayList<>();

        long totalResult = 0l;
        int totalPages = 0;
        Map<String, String> filtersQuery = new HashMap<>();
        if (!CollectionUtils.isEmpty(filterQuery)) {
            filterQuery.stream().forEach(filter -> {
                String filterValue[] = filter.split(":");
                if (filterValue.length >= 2) {
                    if ((filterValue[0].equals(SearchConstants.START_TIME) || filterValue[0].equals(SearchConstants.END_TIME)) && filterValue.length >= 3) {
                        filtersQuery.put(filterValue[0], String.format("%s:%s", filterValue[1], filterValue[2]));
                    } else {
                        filtersQuery.put(filterValue[0], filterValue[1]);
                    }
                }
            });
            String viewBy = filtersQuery.getOrDefault(SearchConstants.VIEW_BY, StringUtils.EMPTY);
            String invoiceNumber = filtersQuery.getOrDefault(SearchConstants.INVOICE_NUMBER, StringUtils.EMPTY);
            String fileName = filtersQuery.getOrDefault(SearchConstants.FILE_NAME, StringUtils.EMPTY);
            String dateOfRun = filtersQuery.getOrDefault(SearchConstants.PAYMENT_DATE_TIME, StringUtils.EMPTY).replaceAll("[^0-9]", "-");
            String startTime = filtersQuery.getOrDefault(SearchConstants.START_TIME, StringUtils.EMPTY);
            String endTime = filtersQuery.getOrDefault(SearchConstants.END_TIME, StringUtils.EMPTY);
            String status = filtersQuery.getOrDefault(SearchConstants.STATUS, StringUtils.EMPTY);
            String reason = filtersQuery.getOrDefault(SearchConstants.REASON, StringUtils.EMPTY);

            if (SearchConstants.ALL_INVOICES.equalsIgnoreCase(viewBy)) {
                List<Sort.Order> orders = SearchUtil.getSortedOrderList(pageable);
                Criteria criteria = SearchUtil.getCriteria(invoiceNumber, fileName, dateOfRun, startTime, endTime, status, reason);
                ProjectionOperation projectOperation =
                        project().and(SearchConstants.INVOICE_NUMBER).as(SearchConstants.INVOICE_NUMBER)
                                .and(SearchConstants.FILE_NAME).as(SearchConstants.FILE_NAME)
                                .and(SearchConstants.RECON_BATCH_ID).as(SearchConstants.RECON_BATCH_ID)
                                .and(SearchConstants.PAYMENT_DATE_TIME).dateAsFormattedString(SearchConstants.D_M_Y).as(SearchConstants.PAYMENT_DATE_TIME).and(SearchConstants.PAYMENT_DATE_TIME)
                                .dateAsFormattedString(SearchConstants.Y_M_D).as(SearchConstants.DATE_FORMAT2).and(SearchConstants.PAYMENT_DATE_TIME).dateAsFormattedString(SearchConstants.M_D_Y).as(SearchConstants.DATE_FORMAT3)
                                .and(SearchConstants.STATUS).as(SearchConstants.STATUS)
                                .and(SearchConstants.START_TIME).dateAsFormattedString(SearchConstants.H_M).as(SearchConstants.START_TIME)
                                .and(SearchConstants.END_TIME).dateAsFormattedString(SearchConstants.H_M).as(SearchConstants.END_TIME)
                                .and(SearchConstants.REASON).applyCondition(ConditionalOperators.IfNull.ifNull(SearchConstants.REASON).then(StringUtils.EMPTY))
                                .and(SearchConstants.ID).as(SearchConstants.ID);

                // Fetch Result With Matching Criteria
                Aggregation resultAggregation =
                        newAggregation(sort(Sort.by(orders)), projectOperation, match(criteria),
                                skip(pageable.getPageNumber() * pageable.getPageSize()), limit(pageable.getPageSize()));

                // Fetch Total Count With Matching Criteria
                Aggregation totalCountAggregationDb = newAggregation(projectOperation, match(criteria));
                AggregationResults<TaxInvoiceDto> groupResults = mongoTemplate.aggregate(resultAggregation, TaxInvoice.class, TaxInvoiceDto.class);
                AggregationResults<TaxInvoiceDto> totalCountResults = mongoTemplate.aggregate(totalCountAggregationDb, TaxInvoice.class, TaxInvoiceDto.class);
                invoices = groupResults.getMappedResults();
                log.info(String.format("Total Result size %s", invoices.size()));

                totalResult = totalCountResults.getMappedResults().stream().map(agg -> agg.getInvoiceNumber()).count();
                log.info(String.format("Total Result size through agg %s", totalResult));
                totalPages = ((int) totalResult / pageable.getPageSize()) + (totalResult % pageable.getPageSize() == 0 ? 0 : 1);
            }
        }

        if(!CollectionUtils.isEmpty(invoices)) {
            invoices = invoices.stream().map(invoice -> {
                invoice.setReason(propertyUtils.getLocalizedReason(invoice.getReason()));
                return invoice;
            }).collect(Collectors.toList());
        }

        SearchResponseDto searchResponseDto = SearchResponseDto.of(invoices, totalPages, totalResult, pageable.getPageNumber() + 1);
        log.info(String.format("Filter Query Searched - %s", filterQuery.stream().collect(Collectors.joining(","))));
        return searchResponseDto;
    }

    @Override
    public boolean saveTaxInvoice(String reconBatchId, String reconFileName) {
        TaxInvoice taxInvoice = TaxInvoice.builder()
                .status(TaxInvoiceStatus.READY_FOR_RUN.name()).reconBatchId(reconBatchId)
                .fileName(reconFileName).paymentDateTime(LocalDateTime.now()).build();
        taxInvoiceRepository.save(taxInvoice);
        return Boolean.TRUE;
    }

    @Override
    public boolean generateTaxInvoice(String invoiceBatchId) {
        TaxInvoice taxInvoice = taxInvoiceRepository.findByInvoiceNumber(invoiceBatchId)
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + invoiceBatchId));

        boolean isSuccess = Boolean.FALSE;
        String reason = StringUtils.EMPTY;
        String invoiceFileName = StringUtils.EMPTY;
        ChannelSftp channelSftp = null;

        try {
            taxInvoice.setStartTime(LocalTime.now());
            taxInvoice.setStatus(TaxInvoiceStatus.IN_PROGRESS.name());
            taxInvoiceRepository.save(taxInvoice);

            Map<String, RiderTaxInvoiceDetails> riderTaxInvoiceDetailsMap = reconciliationServiceClient.getFinalReconciliationDetailsByBatchId(taxInvoice.getReconBatchId());

            if(CollectionUtils.isEmpty(riderTaxInvoiceDetailsMap)) {
                throw new MatchedDataNotFoundException(ErrorConstants.NO_MATCHED_DATA_ERROR_MSG);
            }

            BatchConfigurationDto config = operationServiceClient.getBatchConfiguration();
            ByteArrayOutputStream csvOutputStream = taxInvoiceGenerator.toCsvFile(taxInvoice.getPaymentDateTime(), config, riderTaxInvoiceDetailsMap);
            String currentDateString = CommonUtils.getFormattedDateTime(taxInvoice.getPaymentDateTime(), Constants.DATETIME_FORMAT_YYMMDDHHMMSS);
            invoiceFileName = MessageFormat.format(invoiceFileNameFormat, taxInvoice.getId(), currentDateString);
            log.info("tax invoice file name {}", invoiceFileName);
            ByteArrayOutputStream chkOutputStream = taxInvoiceGenerator.toCheckFile(invoiceFileName, riderTaxInvoiceDetailsMap.size(), csvOutputStream.size());

            try (
                InputStream csvInputStream = new ByteArrayInputStream(csvOutputStream.toByteArray());
                InputStream chkInputStream = new ByteArrayInputStream(chkOutputStream.toByteArray());
            ) {
                String destinationDir = config.getTaxInvoiceFilePath();
                String chkFileName = CommonUtils.replaceLast(invoiceFileName, FileType.CSV.getExtension(), FileType.CHK.getExtension());
                channelSftp = sftpClient.connect();
                log.info("Uploading tax invoice {} & check file {} on sftp. destinationDir: {}", invoiceFileName, chkFileName, destinationDir);
                channelSftp.put(csvInputStream, destinationDir.concat(invoiceFileName));
                channelSftp.put(chkInputStream, destinationDir.concat(chkFileName));
                isSuccess = Boolean.TRUE;
            }
        } catch(ExternalServiceInvocationException | MatchedDataNotFoundException e) {
            log.error("Failed to generate tax invoice. Reason {}", e.getMessage());
            reason = e.getMessage();
        } catch (JSchException | SftpException e) {
            log.error("Error while connecting with sftp", e);
            reason = ErrorConstants.SFTP_CONNECTION_ERROR_MSG;
        }  catch (Exception e) {
            log.error("Error while generating tax invoice", e);
            reason = ErrorConstants.SERVER_ERROR_EX_MSG;
        } finally {
            sftpClient.disconnect(channelSftp);
            taxInvoice.setInvoiceFileName(invoiceFileName);
            taxInvoice.setReason(reason);
            taxInvoice.setEndTime(LocalTime.now());
            taxInvoice.setStatus(isSuccess ? TaxInvoiceStatus.SUCCESS.name() : TaxInvoiceStatus.FAILED.name());
            taxInvoiceRepository.save(taxInvoice);
        }
        return isSuccess;
    }

    public byte[] downloadTaxInvoice(HttpServletResponse response, String invoiceBatchId) throws IOException {
        TaxInvoice taxInvoice = taxInvoiceRepository.findByInvoiceNumber(invoiceBatchId)
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + invoiceBatchId));

        Map<String, RiderTaxInvoiceDetails> riderTaxInvoiceDetailsMap = reconciliationServiceClient.getFinalReconciliationDetailsByBatchId(taxInvoice.getReconBatchId());
        BatchConfigurationDto config = operationServiceClient.getBatchConfiguration();
        ByteArrayOutputStream csvOutputStream = taxInvoiceGenerator.toCsvFile(taxInvoice.getPaymentDateTime(), config, riderTaxInvoiceDetailsMap);
        String currentDateString = CommonUtils.getFormattedDateTime(taxInvoice.getPaymentDateTime(), Constants.DATETIME_FORMAT_YYMMDDHHMMSS);
        String invoiceFileName = MessageFormat.format(invoiceFileNameFormat, taxInvoice.getId(), currentDateString);
        response.setHeader("Content-disposition", "attachment;filename="+invoiceFileName);
        return csvOutputStream.toByteArray();
    }

    @Override
    public TaxInvoiceDto getTaxInvoiceByReconBatchId(String reconBatchId) {
        TaxInvoice taxInvoice = taxInvoiceRepository.findByReconBatchId(reconBatchId)
                .orElseThrow(() -> new DataNotFoundException("Record not found for reconBatchId " + reconBatchId));
        TaxInvoiceDto taxInvoiceDto = TaxInvoiceDto.builder().build();
        BeanUtils.copyProperties(taxInvoice, taxInvoiceDto);
        return taxInvoiceDto;
    }
}
