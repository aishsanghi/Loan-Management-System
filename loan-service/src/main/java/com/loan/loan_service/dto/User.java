package com.loan.loan_service.dto;

import lombok.Data;

@Data
public class User {
    private String customerId;
    private String name;
    private String email;
    private Double annualIncome;
    private String employmentType;
    private String kycStatus;
}
