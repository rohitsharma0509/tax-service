package com.scb.rider.tax.client.impl;

import com.scb.rider.tax.client.OperationFeignClient;
import com.scb.rider.tax.constants.ErrorConstants;
import com.scb.rider.tax.exception.ExternalServiceInvocationException;
import com.scb.rider.tax.model.dto.BatchConfigurationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OperationServiceClient {

    @Autowired
    private OperationFeignClient operationFeignClient;

    public BatchConfigurationDto getBatchConfiguration() {
        try {
            return operationFeignClient.getBatchConfiguration();
        } catch (Exception e) {
            throw new ExternalServiceInvocationException(ErrorConstants.OPS_SERVICE_ERROR_MSG);
        }
    }

}
