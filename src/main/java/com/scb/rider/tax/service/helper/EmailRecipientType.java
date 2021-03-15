package com.scb.rider.tax.service.helper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum EmailRecipientType {
    TO("to"), CC("cc"), BCC("bcc");
    String label;
}
