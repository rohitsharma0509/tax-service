package com.scb.rider.tax.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GLAccountDetailList {
    private String documentNumber;
    private String businessUnitCode;
    private String accountType;
    private String accountNumber;
    private String description;
    private String currencyCode;
}
