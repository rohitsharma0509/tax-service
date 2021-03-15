package com.scb.rider.tax.model.dto;

import com.scb.rider.tax.model.enums.FinalReconciledStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FinalPaymentReconciliationDetails {
    private String id;
    private String batchId;
    private String accountNumber;
    private String rhRiderId;
    private String raRiderId;
    private String rhTransactionType;
    private String raOrderStatus;
    private FinalReconciledStatus status;
    private String rhRiderName;
    private String raRiderName;
    private String rhJobNumber;
    private String raJobNumber;
    private String rhOrderNumber;
    private String raOrderNumber;
    private String rhPaymentAmount;
    private Double raPaymentAmount;
    private Double raJobAmount;
    private Double rhJobAmount;
    private Double mdrValue;
    private Double vatValue;
}