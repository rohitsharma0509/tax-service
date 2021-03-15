package com.scb.rider.tax.service;

import com.scb.rider.tax.exception.ExcelFileGenerationException;
import com.scb.rider.tax.model.dto.FinalPaymentReconciliationDetails;
import com.scb.rider.tax.model.enums.FinalReconciledStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {
    @InjectMocks
    private ExportService exportService;

    @Test
    void throwExceptionBuildEntityExcelDocumentTest() {
        assertThrows(ExcelFileGenerationException.class, () -> exportService.buildEntityExcelDocument(null, null));
    }

    @Test
    void shouldBuildEntityExcelDocumentTest() throws Exception {
        Double totalSecurityDeducted = 23.00;
        List<FinalPaymentReconciliationDetails> data = Arrays.asList(FinalPaymentReconciliationDetails.builder()
                .mdrValue(23.00).vatValue(12.00).raRiderName("ABC").rhRiderName("ABC")
                .rhTransactionType("Sale").accountNumber("12871872212").batchId("RECON000001")
                .raJobAmount(121.00).raJobNumber("1872182").raOrderNumber("989812")
                .raOrderStatus("COMPLETED").raPaymentAmount(1287.00).raRiderId("878721")
                .rhJobAmount(871.00).rhJobNumber("1287").rhOrderNumber("8721")
                .rhPaymentAmount("29.00").rhRiderId("189").status(FinalReconciledStatus.MATCHED).build());
        byte [] response= exportService.buildEntityExcelDocument(data, totalSecurityDeducted);
        assertNotNull(response);
    }
}
