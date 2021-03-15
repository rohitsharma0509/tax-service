package com.scb.rider.tax.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiderProfileDto {
    private String id;
    private String firstName;
    private String lastName;
    private AddressDto address;
    private String dob;
    private String gender;
    private String nationalID;
    private String accountNumber;
    private String phoneNumber;
    private LocalDateTime latestStatusModifiedDate;
    private String reason;
    private String profilePhotoUrl;
    private boolean consentAcceptFlag;
    private boolean dataSharedFlag;
    private String riderId;
    private LocalDateTime createdDate;
    private String profilePhotoExternalUrl;
}
