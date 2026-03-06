package com.loan.user_service.dto;

import lombok.Data;

@Data
public class CustomerRequest {
    private String name;
    private String email;
    private String phone;
    private Double annualIncome;
    private String employmentType;
    private KycDetailsDto kycDetails;
    private LoanRequestDto loanRequest;
}
