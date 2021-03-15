package com.scb.rider.tax.client.impl;

import com.scb.rider.tax.client.RiderFeignClient;
import java.util.Arrays;

import com.scb.rider.tax.exception.ExternalServiceInvocationException;
import com.scb.rider.tax.model.dto.RiderProfileDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiderServiceClientTest {

    private static final String RIDER_ID = "RR00001";

    @InjectMocks
    private RiderServiceClient riderServiceClient;

    @Mock
    private RiderFeignClient riderFeignClient;

    @Test
    void throwExceptionGetRiderProfilesByRiderIds() {
        when(riderFeignClient.getRiderProfilesByRiderIds(anyListOf(String.class))).thenThrow(new NullPointerException());
        List<String> riderIds = Arrays.asList(RIDER_ID);
        assertThrows(ExternalServiceInvocationException.class, () -> riderServiceClient.getRiderProfilesByRiderIds(riderIds));
    }

    @Test
    void shouldGetRiderProfilesByRiderIds() {
        RiderProfileDto riderProfile = RiderProfileDto.builder().riderId(RIDER_ID).build();
        List<RiderProfileDto> riderProfiles = Arrays.asList(riderProfile);
        when(riderFeignClient.getRiderProfilesByRiderIds(anyListOf(String.class))).thenReturn(riderProfiles);
        List<String> riderIds = Arrays.asList(RIDER_ID);
        List<RiderProfileDto> result = riderServiceClient.getRiderProfilesByRiderIds(riderIds);
        assertEquals(RIDER_ID, result.get(0).getRiderId());
    }
}
