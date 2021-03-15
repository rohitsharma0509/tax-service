package com.scb.rider.tax.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RiderDetailsWithSecurityBalance {
    private String riderId;
    private Double remainingSecurityBalance;
    private Double securityAmountDeducted;
}
