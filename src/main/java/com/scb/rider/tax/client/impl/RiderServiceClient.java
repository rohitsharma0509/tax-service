package com.scb.rider.tax.client.impl;

import com.scb.rider.tax.client.RiderFeignClient;
import com.scb.rider.tax.constants.ErrorConstants;
import com.scb.rider.tax.exception.ExternalServiceInvocationException;
import com.scb.rider.tax.model.dto.RiderProfileDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RiderServiceClient {

    @Autowired
    private RiderFeignClient riderFeignClient;

    public List<RiderProfileDto> getRiderProfilesByRiderIds(List<String> riderIds) {
        try {
            return riderFeignClient.getRiderProfilesByRiderIds(riderIds);
        } catch (Exception e) {
            throw new ExternalServiceInvocationException(ErrorConstants.RIDER_SERVICE_ERROR_MSG);
        }
    }

}
