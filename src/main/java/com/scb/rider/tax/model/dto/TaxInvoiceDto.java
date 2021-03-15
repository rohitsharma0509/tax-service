package com.scb.rider.tax.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxInvoiceDto {
    private String id;
    private String invoiceNumber;
    private String reconBatchId;
    private String status;
    private String fileName;
    private String startTime;
    private String endTime;
    private String paymentDateTime;
    private String reason;
}
