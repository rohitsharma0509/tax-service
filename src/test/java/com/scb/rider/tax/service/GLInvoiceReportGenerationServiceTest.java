package com.scb.rider.tax.service;

import com.scb.rider.tax.client.impl.ReconciliationServiceClient;
import com.scb.rider.tax.client.impl.SettlementServiceClient;
import com.scb.rider.tax.exception.ExcelFileGenerationException;
import com.scb.rider.tax.exception.MatchedDataNotFoundException;
import com.scb.rider.tax.exception.RecipientListEmptyException;
import com.scb.rider.tax.model.document.GLInvoice;
import com.scb.rider.tax.repository.GLInvoiceRepository;
import com.scb.rider.tax.service.impl.AmazonS3Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.util.Collections;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GLInvoiceReportGenerationServiceTest {

    private static final String RECON_BATCH_ID = "RECON000001";
    private static final String S1_BATCH_ID = "S1000000001";
    private static final String GL_FILE_NAME = "GL-Invoice.xls";
    private static final String GL_INVOICE = "GL-Invoice";
    private static final int INVOKED_ONCE = 1;
    private static final int INVOKED_TWICE = 2;

    @InjectMocks
    private GLInvoiceReportGenerationService glInvoiceReportGenerationService;

    @Mock
    private EmailService emailService;

    @Mock
    private ReconciliationServiceClient reconciliationServiceClient;

    @Mock
    private GLInvoiceRepository glInvoiceRepository;

    @Mock
    private AmazonS3Service amazonS3Service;

    @Test
    void shouldNotGenerateInvoiceReportWhenExceptionOccurred() {
        GLInvoice glInvoice = GLInvoice.builder().s1BatchId(S1_BATCH_ID).reconBatchId(RECON_BATCH_ID).build();
        when(reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(eq(RECON_BATCH_ID))).thenThrow(new NullPointerException());
        glInvoiceReportGenerationService.generateInvoiceReport(glInvoice);
        verify(glInvoiceRepository, times(INVOKED_TWICE)).save(any(GLInvoice.class));
    }

    @Test
    void shouldNotGenerateInvoiceReportWhenNoMatchedDataFound() {
        GLInvoice glInvoice = GLInvoice.builder().s1BatchId(S1_BATCH_ID).reconBatchId(RECON_BATCH_ID).build();
        when(reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(eq(RECON_BATCH_ID))).thenThrow(new MatchedDataNotFoundException("No matched data"));
        glInvoiceReportGenerationService.generateInvoiceReport(glInvoice);
        verify(glInvoiceRepository, times(INVOKED_TWICE)).save(any(GLInvoice.class));
    }

    @Test
    void shouldNotGenerateInvoiceReportWhenExceptionWhileUploadingToS3() throws Exception {
        GLInvoice glInvoice = GLInvoice.builder().s1BatchId(S1_BATCH_ID).reconBatchId(RECON_BATCH_ID).build();
        when(reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(eq(RECON_BATCH_ID))).thenReturn(Collections.emptyList());
        when(emailService.getFileName()).thenReturn(GL_FILE_NAME);
        when(amazonS3Service.uploadInputStream(any(InputStream.class), eq(GL_INVOICE), eq(S1_BATCH_ID), eq(GL_FILE_NAME))).thenThrow(new NullPointerException());
        glInvoiceReportGenerationService.generateInvoiceReport(glInvoice);
        verify(emailService, times(INVOKED_ONCE)).sendMailWithAttachment(eq(GL_FILE_NAME), any());
        verify(glInvoiceRepository, times(INVOKED_TWICE)).save(any(GLInvoice.class));
    }

    @Test
    void shouldNotGenerateInvoiceReportWhenRecipientListIsEmpty() throws Exception {
        GLInvoice glInvoice = GLInvoice.builder().s1BatchId(S1_BATCH_ID).reconBatchId(RECON_BATCH_ID).build();
        when(reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(eq(RECON_BATCH_ID))).thenReturn(Collections.emptyList());
        when(emailService.getFileName()).thenReturn(GL_FILE_NAME);
        when(amazonS3Service.uploadInputStream(any(InputStream.class), eq(GL_INVOICE), eq(S1_BATCH_ID), eq(GL_FILE_NAME))).thenReturn(GL_FILE_NAME);
        doThrow(new RecipientListEmptyException("ToEmail is null")).when(emailService).sendMailWithAttachment(eq(GL_FILE_NAME), any(byte[].class));
        glInvoiceReportGenerationService.generateInvoiceReport(glInvoice);
        verify(glInvoiceRepository, times(INVOKED_TWICE)).save(any(GLInvoice.class));
    }
}
