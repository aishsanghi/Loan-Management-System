package com.loan.loan_service.dto;
import lombok.Data;

@Data
public class LoanRequest {
    private String userId;
    private Double requestedAmount;
    private Integer tenureMonths;
}
