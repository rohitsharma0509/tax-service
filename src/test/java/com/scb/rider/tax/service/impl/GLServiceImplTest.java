package com.scb.rider.tax.service.impl;

import com.scb.rider.tax.model.document.GLInvoice;
import com.scb.rider.tax.repository.GLInvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GLServiceImplTest {

    private static final String S1_BATCH_ID = "S1000000001";
    private static final int INVOKED_ONCE = 1;

    @InjectMocks
    private GLServiceImpl glServiceImpl;

    @Mock
    private GLInvoiceRepository glInvoiceRepository;

    @Test
    void shouldSaveGLInvoice() {
        GLInvoice glInvoice = GLInvoice.builder().build();
        glServiceImpl.saveGLInvoice(glInvoice);
        verify(glInvoiceRepository, times(INVOKED_ONCE)).save(any(GLInvoice.class));
    }

    @Test
    void shouldGetListByBatchId() {
        GLInvoice glInvoice = GLInvoice.builder().s1BatchId(S1_BATCH_ID).build();
        when(glInvoiceRepository.findByReconBatchIdOrS1BatchId(eq(S1_BATCH_ID), eq(S1_BATCH_ID))).thenReturn(Arrays.asList(glInvoice));
        List<GLInvoice> result = glServiceImpl.getListByBatchId(S1_BATCH_ID);
        assertEquals(S1_BATCH_ID, result.get(0).getS1BatchId());
    }

    @Test
    void shouldGetGLInvoiceByS1BatchId() {
        when(glInvoiceRepository.findByS1BatchId(eq(S1_BATCH_ID))).thenReturn(Optional.empty());
        Optional<GLInvoice> result = glServiceImpl.getGLInvoiceByS1BatchId(S1_BATCH_ID);
        assertFalse(result.isPresent());
    }
}
