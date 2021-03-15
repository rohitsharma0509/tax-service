package com.scb.rider.tax.client.impl;

import com.scb.rider.tax.client.ReconciliationFeignClient;
import com.scb.rider.tax.constants.ErrorConstants;
import com.scb.rider.tax.exception.ExternalServiceInvocationException;
import com.scb.rider.tax.exception.MatchedDataNotFoundException;
import com.scb.rider.tax.model.dto.FinalPaymentReconciliationDetails;
import com.scb.rider.tax.model.dto.RiderTaxInvoiceDetails;
import com.scb.rider.tax.model.enums.FinalReconciledStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Component
public class ReconciliationServiceClient {

    private static final int SIZE = 10000;

    @Autowired
    private ReconciliationFeignClient reconciliationFeignClient;

    public Map<String, RiderTaxInvoiceDetails> getFinalReconciliationDetailsByBatchId(String reconcileBatchId) {
        Page<FinalPaymentReconciliationDetails> page;
        Map<String, RiderTaxInvoiceDetails> riderTaxInvoiceDataMap = new HashMap<>();
        int totalPages = 0;
        do {
            try {
                page = reconciliationFeignClient.getFinalReconciliationDetailsByBatchId(reconcileBatchId, FinalReconciledStatus.MATCHED.name(), totalPages++, SIZE);
            } catch(Exception e) {
                throw new ExternalServiceInvocationException(ErrorConstants.RECONCILIATION_SERVICE_ERROR_MSG);
            }
            putResponseInMap(riderTaxInvoiceDataMap, page);
        } while (page.getTotalPages() > totalPages);
        log.info("number of riders from reconciliation service = {}", riderTaxInvoiceDataMap.size());
        return riderTaxInvoiceDataMap;
    }

    private void putResponseInMap(Map<String, RiderTaxInvoiceDetails> riderTaxInvoiceDataMap, Page<FinalPaymentReconciliationDetails> reconciledPage){
        if(Objects.nonNull(reconciledPage)){
            List<FinalPaymentReconciliationDetails> reconciledPageContent = reconciledPage.getContent();
            for(FinalPaymentReconciliationDetails riderDetails : reconciledPageContent) {
                if (riderTaxInvoiceDataMap.containsKey(riderDetails.getRaRiderId())) {
                    RiderTaxInvoiceDetails existingRiderTaxInvoiceDetails = riderTaxInvoiceDataMap.get(riderDetails.getRaRiderId());
                    double totalMdrValue = existingRiderTaxInvoiceDetails.getTotalMdrValue() + riderDetails.getMdrValue();
                    existingRiderTaxInvoiceDetails.setTotalMdrValue(totalMdrValue);
                    double totalVatValue = existingRiderTaxInvoiceDetails.getTotalVatValue() + riderDetails.getVatValue();
                    existingRiderTaxInvoiceDetails.setTotalVatValue(totalVatValue);
                    double totalPaymentAmount = existingRiderTaxInvoiceDetails.getTotalPaymentAmount() + riderDetails.getRaPaymentAmount();
                    existingRiderTaxInvoiceDetails.setTotalPaymentAmount(totalPaymentAmount);
                } else {
                    RiderTaxInvoiceDetails riderTaxInvoiceDetails = RiderTaxInvoiceDetails.builder()
                            .riderId(riderDetails.getRaRiderId())
                            .riderAccountNumber(riderDetails.getAccountNumber())
                            .riderName(riderDetails.getRaRiderName())
                            .totalPaymentAmount(riderDetails.getRaPaymentAmount())
                            .totalMdrValue(riderDetails.getMdrValue())
                            .totalVatValue(riderDetails.getVatValue())
                            .build();
                    riderTaxInvoiceDataMap.put(riderDetails.getRaRiderId(), riderTaxInvoiceDetails);
                }
            }
        }
    }

    public List<FinalPaymentReconciliationDetails> getFinalReconciliationDetailsByReconBatchId(String reconcileBatchId) {
        Page<FinalPaymentReconciliationDetails> page;
        List<FinalPaymentReconciliationDetails> finalReconciliationList = new ArrayList<>();
        int totalPages = 0;
        do {
            try {
                page = reconciliationFeignClient.getFinalReconciliationDetailsByBatchId(reconcileBatchId, FinalReconciledStatus.MATCHED.name(), totalPages++, SIZE);
            } catch (Exception e) {
                throw new ExternalServiceInvocationException(ErrorConstants.RECONCILIATION_SERVICE_ERROR_MSG);
            }
            finalReconciliationList.addAll(page.getContent());
        } while (page.getTotalPages() > totalPages);
        log.info("number of riders from reconciliation service = {}", finalReconciliationList.size());
        if(CollectionUtils.isEmpty(finalReconciliationList)) {
            throw new MatchedDataNotFoundException(ErrorConstants.NO_MATCHED_DATA_ERROR_MSG);
        }
        return finalReconciliationList;
    }
}
