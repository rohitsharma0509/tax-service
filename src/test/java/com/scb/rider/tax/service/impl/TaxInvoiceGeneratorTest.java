package com.scb.rider.tax.service.impl;

import com.scb.rider.tax.client.impl.RiderServiceClient;
import com.scb.rider.tax.model.dto.AddressDto;
import com.scb.rider.tax.model.dto.BatchConfigurationDto;
import com.scb.rider.tax.model.dto.RiderProfileDto;
import com.scb.rider.tax.model.dto.RiderTaxInvoiceDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaxInvoiceGeneratorTest {

    private static final String RIDER_ID = "RR00001";
    private static final String RIDER_NAME = "test";
    private static final String ACCOUNT_NUMBER = "0494308890";
    private static final String NATIONAL_ID = "62425542";
    private static final String INVOICE_FILE_NAME = "rbh-rider-60261f0ae5d94525e6470625-210212143906.csv";

    @InjectMocks
    private TaxInvoiceGenerator taxInvoiceGenerator;

    @Mock
    private RiderServiceClient riderServiceClient;

    @Test
    void shouldConvertToCsv() throws IOException {
        ReflectionTestUtils.setField(taxInvoiceGenerator, "invoiceNumberFormat", "TINVR{0}00000{1}");
        Map<String, RiderTaxInvoiceDetails> reconDataMap = new HashMap<>();
        reconDataMap.put(RIDER_ID, getRiderTaxDetails());
        List<RiderProfileDto> riderProfiles = new ArrayList<>();
        riderProfiles.add(getRiderProfile());
        when(riderServiceClient.getRiderProfilesByRiderIds(anyListOf(String.class))).thenReturn(riderProfiles);
        ByteArrayOutputStream result = taxInvoiceGenerator.toCsvFile(LocalDateTime.now(), getBatchConfig(), reconDataMap);
        assertNotNull(result);
    }

    @Test
    void shouldConvertToCheckFile() throws IOException {
        ByteArrayOutputStream result = taxInvoiceGenerator.toCheckFile(INVOICE_FILE_NAME, 2, 3000);
        assertNotNull(result);
    }

    private RiderTaxInvoiceDetails getRiderTaxDetails() {
        return RiderTaxInvoiceDetails.builder()
                .riderId(RIDER_ID).riderAccountNumber(ACCOUNT_NUMBER).riderName(RIDER_NAME)
                .totalMdrValue(10.0).totalVatValue(10.0).totalPaymentAmount(100.0).build();
    }

    private BatchConfigurationDto getBatchConfig() {
        return BatchConfigurationDto.builder().mdrPercentage("2.2").vatPercentage("1.2").build();
    }

    private RiderProfileDto getRiderProfile() {
        AddressDto addressDto = AddressDto.builder().floorNumber("1")
                .country("thailand").build();
        return RiderProfileDto.builder().riderId(RIDER_ID).nationalID(NATIONAL_ID)
                .address(addressDto).build();
    }
}
