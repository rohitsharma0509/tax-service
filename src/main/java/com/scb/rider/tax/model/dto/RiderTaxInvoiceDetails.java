package com.scb.rider.tax.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class RiderTaxInvoiceDetails {
    private String riderId;
    private String riderAccountNumber;
    private String riderNationalId;
    private String riderName;
    private Double totalPaymentAmount;
    private Double totalMdrValue;
    private Double totalVatValue;
}
