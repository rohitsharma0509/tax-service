package com.scb.rider.tax.controller;

import com.scb.rider.tax.model.dto.SearchResponseDto;
import com.scb.rider.tax.model.dto.TaxInvoiceDto;
import com.scb.rider.tax.service.TaxInvoiceService;
import com.scb.rider.tax.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tax")
public class TaxInvoiceController {

    @Autowired
    private TaxInvoiceService taxInvoiceService;

    @GetMapping("/search")
    public ResponseEntity<SearchResponseDto> searchTaxInvoicesByColumnFilterQuery(
            @RequestParam(name = "filterquery", required = false) List<String> filterquery, @PageableDefault(page = 0, size = 10)
    @SortDefault.SortDefaults(@SortDefault(sort = "invoiceNumber", direction = Sort.Direction.DESC)) Pageable pageable) {
        return ResponseEntity.ok(taxInvoiceService.searchTaxInvoices(filterquery, pageable));
    }

    @PostMapping("/{reconBatchId}/{reconFileName}")
    public ResponseEntity<Boolean> createTaxInvoice(@PathVariable("reconBatchId") String reconBatchId, @PathVariable("reconFileName") String reconFileName) {
        boolean isSuccess = taxInvoiceService.saveTaxInvoice(reconBatchId, reconFileName);
        return ResponseEntity.ok(isSuccess);
    }

    @PutMapping("/{invoiceBatchId}")
    public ResponseEntity<Boolean> updateTaxInvoice(@PathVariable("invoiceBatchId") String invoiceBatchId) {
        boolean isSuccess = taxInvoiceService.generateTaxInvoice(invoiceBatchId);
        return ResponseEntity.ok(isSuccess);
    }

    @GetMapping("/{invoiceBatchId}/download")
    public ResponseEntity<byte[]> downloadTaxInvoiceByInvoiceId(HttpServletResponse response, @PathVariable("invoiceBatchId") String invoiceBatchId) throws IOException {
        byte[] bytes = taxInvoiceService.downloadTaxInvoice(response, invoiceBatchId);
        CommonUtils.downloadFile(response, bytes);
        return ResponseEntity.ok(bytes);
    }

    @GetMapping("/{reconBatchId}")
    public ResponseEntity<TaxInvoiceDto> getTaxInvoiceByReconBatchId(@PathVariable("reconBatchId") String reconBatchId) {
        return ResponseEntity.ok(taxInvoiceService.getTaxInvoiceByReconBatchId(reconBatchId));
    }

}
