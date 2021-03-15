package com.scb.rider.tax.client;

import com.scb.rider.tax.model.dto.RiderDetailsWithSecurityBalance;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "settlementServiceFeignClient", url = "${rest.client.settlement-service}")
public interface SettlementServiceFeignClient {

    @GetMapping("/api/settlement/batch/security-balance/{batchRef}")
    public List<RiderDetailsWithSecurityBalance> getRiderSecurityBalances(@PathVariable("batchRef") String batchRef);

}
