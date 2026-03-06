package com.loan.kyc_service.dto;

import lombok.Data;

@Data
public class LoanServiceRequest {
    private String userId;
    private Double requestedAmount;
    private Integer tenureMonths;
}
