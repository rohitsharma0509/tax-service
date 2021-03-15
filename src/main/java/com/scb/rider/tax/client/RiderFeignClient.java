package com.scb.rider.tax.client;

import com.scb.rider.tax.model.dto.RiderProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "riderFeignClient", url = "${rest.client.rider-service}")
public interface RiderFeignClient {

    @PostMapping(value = "/profile/details")
    public List<RiderProfileDto> getRiderProfilesByRiderIds(List<String> riderIds);

}
