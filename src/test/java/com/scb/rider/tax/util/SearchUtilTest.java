package com.scb.rider.tax.util;

import com.scb.rider.tax.constants.SearchConstants;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class SearchUtilTest {

    private static final int CONTAINS_ONE = 1;
    private static final String INVOICE_NUMBER = "INV00000001";
    private static final String PAYMENT_DATETIME = "2012-02-12";

    @Test
    void shouldGetSortedOrderListWithDefaultSort() {
        Pageable pageable = PageRequest.of(1, 10, Sort.unsorted());
        List<Sort.Order> result = SearchUtil.getSortedOrderList(pageable);
        assertEquals(CONTAINS_ONE, result.size());
        assertEquals(SearchConstants.INVOICE_NUMBER, result.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, result.get(0).getDirection());
    }

    @Test
    void shouldGetSortedOrderListWithSortOnInvoiceNumberInDescOrder() {
        Pageable pageable = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, SearchConstants.INVOICE_NUMBER));
        List<Sort.Order> result = SearchUtil.getSortedOrderList(pageable);
        assertEquals(CONTAINS_ONE, result.size());
        assertEquals(SearchConstants.INVOICE_NUMBER, result.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, result.get(0).getDirection());
    }

    @Test
    void shouldGetSortedOrderListWithMultiFieldSortingIncludingWrongField() {
        Sort.Order invoiceNumberSortOrder = new Sort.Order(Sort.Direction.ASC, SearchConstants.INVOICE_NUMBER);
        Sort.Order wrongSortOrder = new Sort.Order(Sort.Direction.DESC, SearchConstants.DESC);
        Pageable pageable = PageRequest.of(1, 10, Sort.by(invoiceNumberSortOrder, wrongSortOrder));
        List<Sort.Order> result = SearchUtil.getSortedOrderList(pageable);
        assertEquals(CONTAINS_ONE, result.size());
        assertEquals(SearchConstants.INVOICE_NUMBER, result.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, result.get(0).getDirection());
    }

    @Test
    void shouldGetCriteriaWithDefaultField() {
        Criteria result = SearchUtil.getCriteria(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
        assertNotNull(result);
    }

    @Test
    void shouldGetCriteriaWithInvoiceNumber() {
        Criteria result = SearchUtil.getCriteria(INVOICE_NUMBER, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
        assertNotNull(result);
    }

    @Test
    void shouldGetCriteriaWithPaymentDateTime() {
        Criteria result = SearchUtil.getCriteria(StringUtils.EMPTY, PAYMENT_DATETIME, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
        assertNotNull(result);
    }

}
