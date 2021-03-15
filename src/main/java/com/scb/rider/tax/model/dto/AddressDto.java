package com.scb.rider.tax.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressDto {
    private String landmark;
    private String city;
    private String country;
    private String village;
    private String district;
    private String state;
    private String countryCode;
    private String zipCode;
    private String floorNumber;
    private String unitNumber;

    @Override
    public String toString() {
        List<String> addressFields = Arrays.asList(floorNumber, unitNumber, landmark, village, district, city, state, zipCode, country);
        String address = addressFields.stream().filter(Objects::nonNull).collect(Collectors.joining(StringUtils.SPACE));
        return address.replaceAll(",", StringUtils.EMPTY);
    }
}
