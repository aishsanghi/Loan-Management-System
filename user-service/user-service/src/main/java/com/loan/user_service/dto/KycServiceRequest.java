package com.loan.user_service.dto;
import lombok.Data;

@Data
public class KycServiceRequest {
    private String userId;
    private String aadhaarNumber;
    private String panNumber;
    private Double requestedAmount;
    private Integer tenureMonths;
}
