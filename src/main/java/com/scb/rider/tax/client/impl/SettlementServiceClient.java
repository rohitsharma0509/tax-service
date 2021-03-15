package com.scb.rider.tax.client.impl;

import com.scb.rider.tax.client.SettlementServiceFeignClient;
import com.scb.rider.tax.constants.ErrorConstants;
import com.scb.rider.tax.exception.ExternalServiceInvocationException;
import com.scb.rider.tax.model.dto.RiderDetailsWithSecurityBalance;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Log4j2
public class SettlementServiceClient {

    @Autowired
    private SettlementServiceFeignClient settlementServiceFeignClient;

    public Double getTotalSecurityBalanceDeduction(String batchId) {
        try {
            List<RiderDetailsWithSecurityBalance> securityBalanceList = settlementServiceFeignClient.getRiderSecurityBalances(batchId);
            log.info("Fetch with size:{} ", securityBalanceList.size());
            AtomicReference<Double> totalSecurityDeduction = new AtomicReference<>(0.0);
            securityBalanceList.stream()
                    .forEach(rs -> totalSecurityDeduction.set(
                            totalSecurityDeduction.get() + rs.getSecurityAmountDeducted()));
            return totalSecurityDeduction.get();
        } catch (Exception e) {
            log.error("Exception while fetching record for security balance {}",e.getMessage());
            throw new ExternalServiceInvocationException(ErrorConstants.SETTLEMENT_SERVICE_ERROR_MSG);
        }
    }
}
