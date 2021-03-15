package com.scb.rider.tax.service.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EmailAttachment {
    private byte[] bytes;
    private String name;
    private String description;
    private String disposition;
}
