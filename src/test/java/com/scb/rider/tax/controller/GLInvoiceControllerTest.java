package com.scb.rider.tax.controller;

import com.scb.rider.tax.model.document.GLInvoice;
import com.scb.rider.tax.service.GLInvoiceReportGenerationService;
import com.scb.rider.tax.service.GLInvoiceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GLInvoiceControllerTest {

    private static final String S1_BATCH_ID = "S1000000001";
    private static final int INVOKED_ONCE = 1;

    @InjectMocks
    private GLInvoiceController glInvoiceController;

    @Mock
    private GLInvoiceService glInvoiceService;

    @Mock
    private GLInvoiceReportGenerationService glInvoiceReportGenerationService;

    @Test
    void shouldGetGLInvoiceByBatchIdWhenGLNotPresent() {
        GLInvoice glInvoice = GLInvoice.builder().s1BatchId(S1_BATCH_ID).build();
        when(glInvoiceService.getGLInvoiceByS1BatchId(eq(S1_BATCH_ID))).thenReturn(Optional.empty());
        ResponseEntity<Boolean> result = glInvoiceController.getGLInvoiceByBatchId(glInvoice);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody());
        verify(glInvoiceReportGenerationService, times(INVOKED_ONCE)).generateInvoiceReport(any(GLInvoice.class));
    }

    @Test
    void shouldGetGLInvoiceByBatchIdWhenGLPresent() {
        GLInvoice glInvoice = GLInvoice.builder().s1BatchId(S1_BATCH_ID).build();
        when(glInvoiceService.getGLInvoiceByS1BatchId(eq(S1_BATCH_ID))).thenReturn(Optional.of(glInvoice));
        ResponseEntity<Boolean> result = glInvoiceController.getGLInvoiceByBatchId(glInvoice);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody());
        verify(glInvoiceReportGenerationService, times(INVOKED_ONCE)).generateInvoiceReport(any(GLInvoice.class));
    }

    @Test
    void shouldGetGLInvoiceByBatchId() {
        GLInvoice glInvoice = GLInvoice.builder().s1BatchId(S1_BATCH_ID).build();
        when(glInvoiceService.getListByBatchId(eq(S1_BATCH_ID))).thenReturn(Arrays.asList(glInvoice));
        ResponseEntity<List<GLInvoice>> result = glInvoiceController.getGLInvoiceByBatchId(S1_BATCH_ID);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(S1_BATCH_ID, result.getBody().get(0).getS1BatchId());
    }
}
