package com.scb.rider.tax.client.impl;

import com.scb.rider.tax.client.SettlementServiceFeignClient;
import com.scb.rider.tax.exception.ExternalServiceInvocationException;
import com.scb.rider.tax.model.dto.RiderDetailsWithSecurityBalance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
class SettlementServiceClientTest {

    private static final String RIDER_ID = "RR00001";
    private static final String BATCH_ID = "S1000000001";

    @InjectMocks
    private SettlementServiceClient settlementServiceClient;

    @Mock
    private SettlementServiceFeignClient settlementServiceFeignClient;

    @Test
    void throwExceptionGetTotalSecurityBalanceDeduction() {
        when(settlementServiceFeignClient.getRiderSecurityBalances(eq(BATCH_ID))).thenThrow(new NullPointerException());
        assertThrows(ExternalServiceInvocationException.class, () -> settlementServiceClient.getTotalSecurityBalanceDeduction(BATCH_ID));
    }

    @Test
    void shouldGetTotalSecurityBalanceDeduction() {
        RiderDetailsWithSecurityBalance riderBalanceDetails = new RiderDetailsWithSecurityBalance();
        riderBalanceDetails.setSecurityAmountDeducted(100.0);
        riderBalanceDetails.setRemainingSecurityBalance(50.0);
        riderBalanceDetails.setRiderId(RIDER_ID);
        List<RiderDetailsWithSecurityBalance> securityBalanceList = Arrays.asList(riderBalanceDetails);
        when(settlementServiceFeignClient.getRiderSecurityBalances(eq(BATCH_ID))).thenReturn(securityBalanceList);
        Double result = settlementServiceClient.getTotalSecurityBalanceDeduction(BATCH_ID);
        assertEquals(100.0, result);
    }
}
