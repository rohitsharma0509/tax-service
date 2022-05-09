package com.scb.rider.tax.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.scb.rider.tax.model.document.GLInvoice;
import com.scb.rider.tax.service.GLInvoiceReportGenerationService;
import com.scb.rider.tax.service.GLInvoiceService;
import com.scb.rider.tax.view.View;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/api/gl-invoice")
public class GLInvoiceController {

    @Autowired
    private GLInvoiceService glInvoiceService;

    @Autowired
    private GLInvoiceReportGenerationService glInvoiceReportGenerationService;

    @PostMapping
    public ResponseEntity<Boolean> getGLInvoiceByBatchId(@RequestBody @JsonView(value = View.GLInvoiceView.POST.class)
                                                                 GLInvoice glInvoice) {
        log.info("Getting Reconciliation Details by batch id = {} {}", glInvoice.getS1BatchId(), glInvoice.getReconBatchId());
        Optional<GLInvoice> savedGlInvoice = glInvoiceService.getGLInvoiceByS1BatchId(glInvoice.getS1BatchId());
        if (savedGlInvoice.isPresent()) {
            glInvoiceReportGenerationService.generateInvoiceReport(savedGlInvoice.get());
        } else {
            glInvoiceReportGenerationService.generateInvoiceReport(glInvoice);
        }
        return ResponseEntity.ok(true);
    }

    @GetMapping("/{batchId}")
    @JsonView(value = View.GLInvoiceView.GET.class)
    public ResponseEntity<List<GLInvoice>> getGLInvoiceByBatchId(@PathVariable String batchId) {
        log.info("Getting Reconciliation Details by batch id = {} ", batchId);
        return ResponseEntity.ok(glInvoiceService.getListByBatchId(batchId));
    }
}
