package com.loan.loan_service.dto;

import lombok.Data;

@Data
public class KycStatusResponse {
    private String userId;
    private String status;   // VERIFIED, PENDING, REJECTED
    private String message;
}
