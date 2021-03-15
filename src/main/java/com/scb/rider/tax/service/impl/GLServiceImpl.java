package com.scb.rider.tax.service.impl;

import com.scb.rider.tax.model.document.GLInvoice;
import com.scb.rider.tax.repository.GLInvoiceRepository;
import com.scb.rider.tax.service.GLInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GLServiceImpl implements GLInvoiceService {
    @Autowired
    private GLInvoiceRepository glInvoiceRepository;

    @Override
    public GLInvoice saveGLInvoice(GLInvoice glInvoice) {
        return glInvoiceRepository.save(glInvoice);
    }

    @Override
    public List<GLInvoice> getListByBatchId(String batchId) {
        return glInvoiceRepository.findByReconBatchIdOrS1BatchId(batchId, batchId);
    }

    @Override
    public Optional<GLInvoice> getGLInvoiceByS1BatchId(String s1batchId) {
        return glInvoiceRepository.findByS1BatchId(s1batchId);
    }
}
