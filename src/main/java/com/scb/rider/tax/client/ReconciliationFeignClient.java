package com.scb.rider.tax.client;

import com.scb.rider.tax.model.dto.CustomPageImpl;
import com.scb.rider.tax.model.dto.FinalPaymentReconciliationDetails;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "reconciliationFeignClient", url = "${rest.client.reconciliation-service}")
public interface ReconciliationFeignClient {

    @GetMapping("/api/reconciliation/batch-details/{batchId}")
    public CustomPageImpl<FinalPaymentReconciliationDetails> getFinalReconciliationDetailsByBatchId(
            @PathVariable("batchId") String batchId,
            @RequestParam(name="status", defaultValue = "MATCHED") String status,
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "10") int size);

}
