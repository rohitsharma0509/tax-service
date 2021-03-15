package com.scb.rider.tax.event;

import com.scb.rider.tax.model.document.TaxInvoice;
import com.scb.rider.tax.service.impl.SequenceGeneratorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class TaxInvoiceEventListener extends AbstractMongoEventListener<TaxInvoice> {

    @Value("${tax-invoice.database.sequence.prefix}")
    private String prefix;

    public static final String SEQUENCE_NAME = "tax_invoice_sequence";

    private SequenceGeneratorService sequenceGenerator;

    @Autowired
    public TaxInvoiceEventListener(SequenceGeneratorService sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<TaxInvoice> event) {
       log.info("onBeforeConvert Event ");
        if (event.getSource().getInvoiceNumber() == null) {
            event.getSource().setInvoiceNumber(prefix + sequenceGenerator.generateSequence(SEQUENCE_NAME));
        }
    }

}
