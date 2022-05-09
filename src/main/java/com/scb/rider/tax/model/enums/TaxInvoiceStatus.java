package com.scb.rider.tax.model.enums;

import java.util.Arrays;
import java.util.List;

public enum TaxInvoiceStatus {
    READY_FOR_RUN, IN_PROGRESS, SUCCESS, FAILED;

    public static List<TaxInvoiceStatus> valuesForEmail() {
        return Arrays.asList(IN_PROGRESS, SUCCESS, FAILED);
    }
}
