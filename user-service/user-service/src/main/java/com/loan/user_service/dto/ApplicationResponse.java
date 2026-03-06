package com.loan.user_service.dto;
import lombok.Data;

@Data
public class ApplicationResponse {
    private String userId;
    private String loanId;
    private String offerId;
    private String kycStatus;
    private Double offeredAmount;
    private Double emiAmount;
    private Double interestRate;
    private Integer tenureMonths;
    private String loanStatus;
    private String message;
}
