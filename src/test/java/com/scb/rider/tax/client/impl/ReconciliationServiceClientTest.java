package com.scb.rider.tax.client.impl;

import com.scb.rider.tax.client.ReconciliationFeignClient;
import com.scb.rider.tax.exception.ExternalServiceInvocationException;
import com.scb.rider.tax.exception.MatchedDataNotFoundException;
import com.scb.rider.tax.model.dto.CustomPageImpl;
import com.scb.rider.tax.model.dto.FinalPaymentReconciliationDetails;
import com.scb.rider.tax.model.dto.RiderTaxInvoiceDetails;
import com.scb.rider.tax.model.enums.FinalReconciledStatus;
import edu.emory.mathcs.backport.java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceClientTest {

    private static final String RECON_BATCH_ID = "RECON00000001";
    private static final String RIDER_ID = "RR00001";
    private static final int PAGE_NO = 0;
    private static final int PAGE_SIZE = 10000;

    @InjectMocks
    private ReconciliationServiceClient reconciliationServiceClient;

    @Mock
    private ReconciliationFeignClient reconciliationFeignClient;

    @Test
    void throwExceptionGetFinalReconciliationDetailsByBatchId() {
        when(reconciliationFeignClient.getFinalReconciliationDetailsByBatchId(eq(RECON_BATCH_ID),
                eq(FinalReconciledStatus.MATCHED.name()), eq(PAGE_NO), eq(PAGE_SIZE))).thenThrow(new NullPointerException());
        assertThrows(ExternalServiceInvocationException.class, () -> reconciliationServiceClient.getFinalReconciliationDetailsByBatchId(RECON_BATCH_ID));
    }

    @Test
    void shouldGetFinalReconciliationDetailsByBatchId() {
        CustomPageImpl<FinalPaymentReconciliationDetails> page = getPage(1, 2l, getFinalPaymentList());
        when(reconciliationFeignClient.getFinalReconciliationDetailsByBatchId(eq(RECON_BATCH_ID),
                eq(FinalReconciledStatus.MATCHED.name()), eq(PAGE_NO), eq(PAGE_SIZE))).thenReturn(page);
        Map<String, RiderTaxInvoiceDetails> result = reconciliationServiceClient.getFinalReconciliationDetailsByBatchId(RECON_BATCH_ID);
        assertEquals(1, result.size());
        assertEquals(20.0, result.get(RIDER_ID).getTotalMdrValue());
        assertEquals(20.0, result.get(RIDER_ID).getTotalMdrValue());
        assertEquals(200.0, result.get(RIDER_ID).getTotalPaymentAmount());
    }

    @Test
    void throwExceptionGetFinalReconciliationDetailsByReconBatchId() {
        when(reconciliationFeignClient.getFinalReconciliationDetailsByBatchId(eq(RECON_BATCH_ID),
                eq(FinalReconciledStatus.MATCHED.name()), eq(PAGE_NO), eq(PAGE_SIZE))).thenThrow(new NullPointerException());
        assertThrows(ExternalServiceInvocationException.class, () -> reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(RECON_BATCH_ID));
    }

    @Test
    void shouldGetFinalReconciliationDetailsByReconBatchId() {
        CustomPageImpl<FinalPaymentReconciliationDetails> page = getPage(1, 2l, getFinalPaymentList());
        when(reconciliationFeignClient.getFinalReconciliationDetailsByBatchId(eq(RECON_BATCH_ID),
                eq(FinalReconciledStatus.MATCHED.name()), eq(PAGE_NO), eq(PAGE_SIZE))).thenReturn(page);
        List<FinalPaymentReconciliationDetails> result = reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(RECON_BATCH_ID);
        assertEquals(2, result.size());
    }

    @Test
    void throwExceptionGetFinalReconciliationDetailsByReconBatchIdWhenMatchedDataNotFound() {
        CustomPageImpl<FinalPaymentReconciliationDetails> page = getPage(1, 2l, Collections.emptyList());
        when(reconciliationFeignClient.getFinalReconciliationDetailsByBatchId(eq(RECON_BATCH_ID),
                eq(FinalReconciledStatus.MATCHED.name()), eq(PAGE_NO), eq(PAGE_SIZE))).thenReturn(page);
        assertThrows(MatchedDataNotFoundException.class, () -> reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(RECON_BATCH_ID));
    }

    private List<FinalPaymentReconciliationDetails> getFinalPaymentList() {
        List<FinalPaymentReconciliationDetails> finalPayments = new ArrayList<>();
        finalPayments.add(getRiderPaymentDetails());
        finalPayments.add(getRiderPaymentDetails());
        return finalPayments;
    }

    private CustomPageImpl<FinalPaymentReconciliationDetails> getPage(int totalPages, long totalElements, List<FinalPaymentReconciliationDetails> finalPayments) {
        CustomPageImpl<FinalPaymentReconciliationDetails> page = new CustomPageImpl<>();
        page.setTotalPages(totalPages);
        page.setTotalElements(totalElements);
        page.setContent(finalPayments);
        return page;
    }

    private FinalPaymentReconciliationDetails getRiderPaymentDetails() {
        return FinalPaymentReconciliationDetails.builder().raRiderId(RIDER_ID)
                .mdrValue(10.0).vatValue(10.0).raPaymentAmount(100.0).build();
    }
}
