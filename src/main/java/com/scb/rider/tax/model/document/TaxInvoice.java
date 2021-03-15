package com.scb.rider.tax.model.document;

import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tax_invoice")
public class TaxInvoice extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -4617737054207095312L;

    @Id
    private String id;

    @Indexed(unique = true, sparse = true)
    private String invoiceNumber;

    private String reconBatchId;

    private String status;

    private String fileName;

    private String invoiceFileName;

    private LocalTime startTime;

    private LocalTime endTime;

    private LocalDateTime paymentDateTime;

    private String reason;

}

