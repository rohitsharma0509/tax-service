package com.scb.rider.tax.service.impl;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.scb.rider.tax.client.impl.OperationServiceClient;
import com.scb.rider.tax.client.impl.ReconciliationServiceClient;
import com.scb.rider.tax.client.impl.SftpClient;
import com.scb.rider.tax.constants.SearchConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.scb.rider.tax.exception.DataNotFoundException;
import com.scb.rider.tax.model.document.TaxInvoice;
import com.scb.rider.tax.model.dto.BatchConfigurationDto;
import com.scb.rider.tax.model.dto.RiderTaxInvoiceDetails;
import com.scb.rider.tax.model.dto.SearchResponseDto;
import com.scb.rider.tax.model.dto.TaxInvoiceDto;
import com.scb.rider.tax.repository.TaxInvoiceRepository;
import com.scb.rider.tax.util.PropertyUtils;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMapOf;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaxInvoiceServiceImplTest {
    private static final String ID = "60261f0ae5d94525e6470625";
    private static final String RIDER_ID = "RR00001";
    private static final String INVOICE_NUMBER = "INV00000001";
    private static final String RECON_BATCH_ID = "RECON000001";
    private static final String RECON_FILE_NAME = "recon.xls";
    private static final String INVOICE_PATH = "rbhhome/invoice";
    private static final String INVOICE_FILE_NAME_FORMAT_FIELD = "invoiceFileNameFormat";
    private static final String INVOICE_FILE_NAME_FORMAT = "rbh-rider-{0}-{1}.csv";
    private static final int INVOKED_ONCE = 1;
    private static final int INVOKED_TWICE = 2;
    @InjectMocks
    private TaxInvoiceServiceImpl taxInvoiceServiceImpl;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private PropertyUtils propertyUtils;
    @Mock
    private TaxInvoiceRepository taxInvoiceRepository;
    @Mock
    private ReconciliationServiceClient reconciliationServiceClient;
    @Mock
    private SftpClient sftpClient;
    @Mock
    private OperationServiceClient operationServiceClient;
    @Mock
    private TaxInvoiceGenerator taxInvoiceGenerator;
    @Mock
    private ChannelSftp channelSftp;

    @Test
    void shouldSearchTaxInvoices() {
        Document rawResults = new Document();
        List<TaxInvoiceDto> mappedResults = new ArrayList<>();
        TaxInvoiceDto taxInvoiceDto = TaxInvoiceDto.builder().invoiceNumber(INVOICE_NUMBER).build();
        mappedResults.add(taxInvoiceDto);
        AggregationResults<TaxInvoiceDto> groupResults = new AggregationResults<>(mappedResults, rawResults);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq(TaxInvoice.class), eq(TaxInvoiceDto.class))).thenReturn(groupResults);
        Pageable pageable = PageRequest.of(1, 10, Sort.by(Sort.Direction.ASC, SearchConstants.INVOICE_NUMBER));
        List<String> filterQuery = Arrays.asList("viewBy:allInvoices", "invoiceNumber:INV000000004", "status:SUCCESS", "fileName:RECON", "startTime:13:24", "endTime:13:24", "paymentDateTime:12-02-2021", "reason:missing");
        SearchResponseDto result = taxInvoiceServiceImpl.searchTaxInvoices(filterQuery, pageable);
        assertEquals(INVOICE_NUMBER, result.getContent().get(0).getInvoiceNumber());
        verifyZeroInteractions(taxInvoiceRepository, reconciliationServiceClient, sftpClient, operationServiceClient, taxInvoiceGenerator, channelSftp);
    }

    @Test
    void shouldSaveTaxInvoice() {
        boolean result = taxInvoiceServiceImpl.saveTaxInvoice(RECON_BATCH_ID, RECON_FILE_NAME);
        assertTrue(result);
        verify(taxInvoiceRepository, times(INVOKED_ONCE)).save(any(TaxInvoice.class));
        verifyZeroInteractions(mongoTemplate, propertyUtils, reconciliationServiceClient, sftpClient, operationServiceClient, taxInvoiceGenerator, channelSftp);
    }

    @Test
    void shouldNotGenerateTaxInvoiceWhenDataNotFound() {
        when(taxInvoiceRepository.findByInvoiceNumber(eq(INVOICE_NUMBER))).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, () -> taxInvoiceServiceImpl.generateTaxInvoice(INVOICE_NUMBER));
        verifyZeroInteractions(mongoTemplate, propertyUtils, reconciliationServiceClient, sftpClient, operationServiceClient, taxInvoiceGenerator, channelSftp);
    }

    @Test
    void shouldNotGenerateTaxInvoiceWhenMatchedDataNotFound() {
        TaxInvoice taxInvoice = TaxInvoice.builder().reconBatchId(RECON_BATCH_ID)
                .invoiceNumber(INVOICE_NUMBER).paymentDateTime(LocalDateTime.now()).build();
        when(taxInvoiceRepository.findByInvoiceNumber(eq(INVOICE_NUMBER))).thenReturn(Optional.of(taxInvoice));
        when(reconciliationServiceClient.getFinalReconciliationDetailsByBatchId(eq(RECON_BATCH_ID))).thenReturn(Collections.emptyMap());
        boolean result = taxInvoiceServiceImpl.generateTaxInvoice(INVOICE_NUMBER);
        assertFalse(result);
        verify(sftpClient, times(INVOKED_ONCE)).disconnect(any());
        verify(taxInvoiceRepository, times(INVOKED_TWICE)).save(any(TaxInvoice.class));
        verifyZeroInteractions(mongoTemplate, propertyUtils, operationServiceClient, taxInvoiceGenerator, channelSftp);
    }

    @Test
    void shouldNotGenerateTaxInvoiceWhenExceptionOccurred() {
        TaxInvoice taxInvoice = TaxInvoice.builder().reconBatchId(RECON_BATCH_ID)
                .invoiceNumber(INVOICE_NUMBER).paymentDateTime(LocalDateTime.now()).build();
        when(taxInvoiceRepository.findByInvoiceNumber(eq(INVOICE_NUMBER))).thenReturn(Optional.of(taxInvoice));
        when(reconciliationServiceClient.getFinalReconciliationDetailsByBatchId(eq(RECON_BATCH_ID))).thenThrow(new NullPointerException());
        boolean result = taxInvoiceServiceImpl.generateTaxInvoice(INVOICE_NUMBER);
        assertFalse(result);
        verify(sftpClient, times(INVOKED_ONCE)).disconnect(any());
        verify(taxInvoiceRepository, times(INVOKED_TWICE)).save(any(TaxInvoice.class));
        verifyZeroInteractions(mongoTemplate, propertyUtils, operationServiceClient, taxInvoiceGenerator, channelSftp);
    }

    @Test
    void shouldNotGenerateTaxInvoiceWhenUnableToConnectWithSftp() throws IOException, JSchException {
        ReflectionTestUtils.setField(taxInvoiceServiceImpl, INVOICE_FILE_NAME_FORMAT_FIELD, INVOICE_FILE_NAME_FORMAT);
        TaxInvoice taxInvoice = TaxInvoice.builder().reconBatchId(RECON_BATCH_ID).id(ID)
                .invoiceNumber(INVOICE_NUMBER).paymentDateTime(LocalDateTime.now()).build();
        when(taxInvoiceRepository.findByInvoiceNumber(eq(INVOICE_NUMBER))).thenReturn(Optional.of(taxInvoice));
        Map<String, RiderTaxInvoiceDetails> reconDataMap = new HashMap<>();
        RiderTaxInvoiceDetails riderTaxInvoiceDetails = RiderTaxInvoiceDetails.builder().riderId(RIDER_ID).build();
        reconDataMap.put(RIDER_ID, riderTaxInvoiceDetails);
        when(reconciliationServiceClient.getFinalReconciliationDetailsByBatchId(eq(RECON_BATCH_ID))).thenReturn(reconDataMap);
        BatchConfigurationDto config = BatchConfigurationDto.builder().taxInvoiceFilePath(INVOICE_PATH).build();
        when(operationServiceClient.getBatchConfiguration()).thenReturn(config);
        when(taxInvoiceGenerator.toCsvFile(any(LocalDateTime.class), any(BatchConfigurationDto.class), anyMapOf(String.class, RiderTaxInvoiceDetails.class))).thenReturn(new ByteArrayOutputStream());
        when(taxInvoiceGenerator.toCheckFile(anyString(), eq(1), anyInt())).thenReturn(new ByteArrayOutputStream());
        when(sftpClient.connect()).thenThrow(new JSchException());
        boolean result = taxInvoiceServiceImpl.generateTaxInvoice(INVOICE_NUMBER);
        assertFalse(result);
        verify(sftpClient, times(INVOKED_ONCE)).disconnect(any());
        verify(taxInvoiceRepository, times(INVOKED_TWICE)).save(any(TaxInvoice.class));
        verifyZeroInteractions(mongoTemplate, propertyUtils, channelSftp);
    }

    @Test
    void shouldGenerateTaxInvoiceSuccessCase() throws IOException, JSchException, SftpException {
        ReflectionTestUtils.setField(taxInvoiceServiceImpl, INVOICE_FILE_NAME_FORMAT_FIELD, INVOICE_FILE_NAME_FORMAT);
        TaxInvoice taxInvoice = TaxInvoice.builder().reconBatchId(RECON_BATCH_ID).id(ID)
                .invoiceNumber(INVOICE_NUMBER).paymentDateTime(LocalDateTime.now()).build();
        when(taxInvoiceRepository.findByInvoiceNumber(eq(INVOICE_NUMBER))).thenReturn(Optional.of(taxInvoice));
        Map<String, RiderTaxInvoiceDetails> reconDataMap = new HashMap<>();
        RiderTaxInvoiceDetails riderTaxInvoiceDetails = RiderTaxInvoiceDetails.builder().riderId(RIDER_ID).build();
        reconDataMap.put(RIDER_ID, riderTaxInvoiceDetails);
        when(reconciliationServiceClient.getFinalReconciliationDetailsByBatchId(eq(RECON_BATCH_ID))).thenReturn(reconDataMap);
        BatchConfigurationDto config = BatchConfigurationDto.builder().taxInvoiceFilePath(INVOICE_PATH).build();
        when(operationServiceClient.getBatchConfiguration()).thenReturn(config);
        when(taxInvoiceGenerator.toCsvFile(any(LocalDateTime.class), any(BatchConfigurationDto.class), anyMapOf(String.class, RiderTaxInvoiceDetails.class))).thenReturn(new ByteArrayOutputStream());
        when(taxInvoiceGenerator.toCheckFile(anyString(), eq(1), anyInt())).thenReturn(new ByteArrayOutputStream());
        when(sftpClient.connect()).thenReturn(channelSftp);
        boolean result = taxInvoiceServiceImpl.generateTaxInvoice(INVOICE_NUMBER);
        assertTrue(result);
        verify(channelSftp, times(INVOKED_TWICE)).put(any(InputStream.class), anyString());
        verify(sftpClient, times(INVOKED_ONCE)).disconnect(any());
        verify(taxInvoiceRepository, times(INVOKED_TWICE)).save(any(TaxInvoice.class));
        verifyZeroInteractions(mongoTemplate, propertyUtils);
    }

    @Test
    void shouldNotDownloadTaxInvoiceWhenInvoiceBatchNotFound() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(taxInvoiceRepository.findByInvoiceNumber(eq(INVOICE_NUMBER))).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, () -> taxInvoiceServiceImpl.downloadTaxInvoice(response, INVOICE_NUMBER));
        verifyZeroInteractions(mongoTemplate, propertyUtils, reconciliationServiceClient, sftpClient, operationServiceClient, taxInvoiceGenerator, channelSftp);
    }

    @Test
    void shouldDownloadTaxInvoice() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ReflectionTestUtils.setField(taxInvoiceServiceImpl, INVOICE_FILE_NAME_FORMAT_FIELD, INVOICE_FILE_NAME_FORMAT);
        TaxInvoice taxInvoice = TaxInvoice.builder().reconBatchId(RECON_BATCH_ID).id(ID)
                .invoiceNumber(INVOICE_NUMBER).paymentDateTime(LocalDateTime.now()).build();
        when(taxInvoiceRepository.findByInvoiceNumber(eq(INVOICE_NUMBER))).thenReturn(Optional.of(taxInvoice));
        Map<String, RiderTaxInvoiceDetails> reconDataMap = new HashMap<>();
        RiderTaxInvoiceDetails riderTaxInvoiceDetails = RiderTaxInvoiceDetails.builder().riderId(RIDER_ID).build();
        reconDataMap.put(RIDER_ID, riderTaxInvoiceDetails);
        when(reconciliationServiceClient.getFinalReconciliationDetailsByBatchId(eq(RECON_BATCH_ID))).thenReturn(reconDataMap);
        BatchConfigurationDto config = BatchConfigurationDto.builder().taxInvoiceFilePath(INVOICE_PATH).build();
        when(operationServiceClient.getBatchConfiguration()).thenReturn(config);
        when(taxInvoiceGenerator.toCsvFile(any(LocalDateTime.class), any(BatchConfigurationDto.class), anyMapOf(String.class, RiderTaxInvoiceDetails.class))).thenReturn(new ByteArrayOutputStream());
        byte[] result = taxInvoiceServiceImpl.downloadTaxInvoice(response, INVOICE_NUMBER);
        assertNotNull(result);
        verifyZeroInteractions(mongoTemplate, propertyUtils, sftpClient, taxInvoiceGenerator);
    }

    @Test
    void throwExceptionGetTaxInvoiceByReconBatchIdWhenNoRecordFound() {
        when(taxInvoiceRepository.findByReconBatchId(eq(RECON_BATCH_ID))).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, () -> taxInvoiceServiceImpl.getTaxInvoiceByReconBatchId(RECON_BATCH_ID));
    }

    @Test
    void shouldGetTaxInvoiceByReconBatchIdWhenNoRecordFound() {
        TaxInvoice taxInvoice = TaxInvoice.builder().invoiceNumber(INVOICE_NUMBER).build();
        when(taxInvoiceRepository.findByReconBatchId(eq(RECON_BATCH_ID))).thenReturn(Optional.of(taxInvoice));
        TaxInvoiceDto result = taxInvoiceServiceImpl.getTaxInvoiceByReconBatchId(RECON_BATCH_ID);
        assertEquals(INVOICE_NUMBER, result.getInvoiceNumber());
    }
}
