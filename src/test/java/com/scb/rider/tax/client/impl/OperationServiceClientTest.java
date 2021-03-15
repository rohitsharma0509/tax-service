package com.scb.rider.tax.client.impl;

import com.scb.rider.tax.client.OperationFeignClient;
import com.scb.rider.tax.exception.ExternalServiceInvocationException;
import com.scb.rider.tax.model.dto.BatchConfigurationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationServiceClientTest {

    private static final String COMPANY_ID = "RBH0001";

    @InjectMocks
    private OperationServiceClient operationServiceClient;

    @Mock
    private OperationFeignClient operationFeignClient;

    @Test
    void throwExceptionGetBatchConfiguration() {
        when(operationFeignClient.getBatchConfiguration()).thenThrow(new NullPointerException());
        assertThrows(ExternalServiceInvocationException.class, () -> operationServiceClient.getBatchConfiguration());
    }

    @Test
    void shouldGetBatchConfiguration() {
        BatchConfigurationDto config = BatchConfigurationDto.builder().companyId(COMPANY_ID).build();
        when(operationFeignClient.getBatchConfiguration()).thenReturn(config);
        BatchConfigurationDto result = operationServiceClient.getBatchConfiguration();
        assertEquals(COMPANY_ID, result.getCompanyId());
    }
}

