package com.loan.kyc_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycServiceRequest {
    private String userId;
    private String aadhaarNumber;
    private String panNumber;
    private Double requestedAmount;
    private Integer tenureMonths;
}
