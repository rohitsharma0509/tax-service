package com.scb.rider.tax.service;

import com.scb.rider.tax.model.document.GLInvoice;

import java.util.List;
import java.util.Optional;

public interface GLInvoiceService {
    GLInvoice saveGLInvoice(GLInvoice glInvoice);
    List<GLInvoice> getListByBatchId(String batchId);
    Optional<GLInvoice> getGLInvoiceByS1BatchId(String s1batchId);
}
