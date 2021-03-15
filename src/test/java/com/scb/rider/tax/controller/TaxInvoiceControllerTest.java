package com.scb.rider.tax.controller;

import com.scb.rider.tax.model.dto.SearchResponseDto;
import com.scb.rider.tax.model.dto.TaxInvoiceDto;
import com.scb.rider.tax.service.TaxInvoiceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaxInvoiceControllerTest {

    private static final String RECON_BATCH_ID = "RECON000001";
    private static final String INVOICE_NUMBER = "INV00000001";
    private static final String RECON_FILE_NAME = "recon.xls";

    @InjectMocks
    private TaxInvoiceController taxInvoiceController;

    @Mock
    private TaxInvoiceService taxInvoiceService;

    @Test
    void shouldSearchTaxInvoicesByColumnFilterQuery() {
        SearchResponseDto searchResponseDto = SearchResponseDto.builder()
                .totalCount(1l).totalPages(1).build();
        when(taxInvoiceService.searchTaxInvoices(anyListOf(String.class), any(Pageable.class))).thenReturn(searchResponseDto);
        Pageable page = PageRequest.of(1, 10, Sort.unsorted());
        ResponseEntity<SearchResponseDto> result = taxInvoiceController.searchTaxInvoicesByColumnFilterQuery(new ArrayList<>(), page);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalCount());
    }

    @Test
    void shouldCreateTaxInvoice() {
        when(taxInvoiceService.saveTaxInvoice(eq(RECON_BATCH_ID), eq(RECON_FILE_NAME))).thenReturn(true);
        ResponseEntity<Boolean> result = taxInvoiceController.createTaxInvoice(RECON_BATCH_ID, RECON_FILE_NAME);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody());
    }

    @Test
    void shouldUpdateTaxInvoice() {
        when(taxInvoiceService.generateTaxInvoice(eq(INVOICE_NUMBER))).thenReturn(true);
        ResponseEntity<Boolean> result = taxInvoiceController.updateTaxInvoice(INVOICE_NUMBER);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody());
    }
    
    @Test
    void shouldDownloadTaxInvoiceByInvoiceId() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(taxInvoiceService.downloadTaxInvoice(any(HttpServletResponse.class), eq(INVOICE_NUMBER))).thenReturn(new byte[1]);
        ResponseEntity<byte[]> result = taxInvoiceController.downloadTaxInvoiceByInvoiceId(response, INVOICE_NUMBER);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result);
    }

    @Test
    void shouldGetTaxInvoiceByReconBatchId() {
        TaxInvoiceDto taxInvoiceDto = TaxInvoiceDto.builder().invoiceNumber(INVOICE_NUMBER).build();
        when(taxInvoiceService.getTaxInvoiceByReconBatchId(eq(RECON_BATCH_ID))).thenReturn(taxInvoiceDto);
        ResponseEntity<TaxInvoiceDto> result = taxInvoiceController.getTaxInvoiceByReconBatchId(RECON_BATCH_ID);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(INVOICE_NUMBER, result.getBody().getInvoiceNumber());
    }
}
