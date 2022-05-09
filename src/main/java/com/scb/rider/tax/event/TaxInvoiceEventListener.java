package com.scb.rider.tax.event;

import com.scb.rider.tax.exception.DataNotFoundException;
import com.scb.rider.tax.model.document.TaxInvoice;
import com.scb.rider.tax.model.enums.TaxInvoiceStatus;
import com.scb.rider.tax.service.TaxInvoiceService;
import com.scb.rider.tax.service.impl.SequenceGeneratorService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Log4j2
public class TaxInvoiceEventListener extends AbstractMongoEventListener<TaxInvoice> {

    private static final int MAX_ATTEMPTS = 3;

    @Value("${tax-invoice.database.sequence.prefix}")
    private String prefix;

    public static final String SEQUENCE_NAME = "tax_invoice_sequence";

    @Autowired
    private SequenceGeneratorService sequenceGenerator;

    @Autowired
    private TaxInvoiceService taxInvoiceService;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<TaxInvoice> event) {
       log.info("onBeforeConvert Event ");
        if (event.getSource().getInvoiceNumber() == null) {
            event.getSource().setInvoiceNumber(prefix + sequenceGenerator.generateSequence(SEQUENCE_NAME));
        }
    }

    @SneakyThrows
    @Override
    public void onAfterSave(AfterSaveEvent<TaxInvoice> event) {
        TaxInvoice taxInvoice = event.getSource();
        if(Objects.isNull(taxInvoice)) {
            return;
        }

        if(TaxInvoiceStatus.READY_FOR_RUN.name().equals(taxInvoice.getStatus())) {
            log.info("generating tax invoice for batchId {}", taxInvoice.getInvoiceNumber());
            triggerTaxInvoiceBatchWithRetry(taxInvoice.getInvoiceNumber());
        }
    }

    public void triggerTaxInvoiceBatchWithRetry(String invoiceNumber) throws InterruptedException {
        for (int count = 0; count < MAX_ATTEMPTS; count++) {
            try {
                taxInvoiceService.generateTaxInvoice(invoiceNumber);
                break;
            } catch (DataNotFoundException e) {
                log.info("Data not found exception for invoiceNumber {}, retryCount: {}", invoiceNumber, count);
                log.error("Exception occurred while generating tax invoice", e);
                Thread.sleep(1000);
            }
        }
    }
}
