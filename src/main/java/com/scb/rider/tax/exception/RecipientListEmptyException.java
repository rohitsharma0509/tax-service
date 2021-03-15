package com.scb.rider.tax.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecipientListEmptyException extends RuntimeException {
    private String errorMessage;
}
