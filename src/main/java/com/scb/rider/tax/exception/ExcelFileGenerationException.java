package com.scb.rider.tax.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExcelFileGenerationException  extends RuntimeException {
    private String errorMessage;
}
