package com.scb.rider.tax.service;

import com.scb.rider.tax.model.dto.SearchResponseDto;
import com.scb.rider.tax.model.dto.TaxInvoiceDto;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface TaxInvoiceService {

    SearchResponseDto searchTaxInvoices(List<String> filterQuery, Pageable pageable);

    boolean saveTaxInvoice(String reconcileBatchId, String reconFileName);

    boolean generateTaxInvoice(String invoiceBatchId);

    byte[] downloadTaxInvoice(HttpServletResponse response, String invoiceBatchId) throws IOException;

    TaxInvoiceDto getTaxInvoiceByReconBatchId(String reconBatchId);
}
