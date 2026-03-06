package com.loan.loan_service.dto;
import lombok.Data;

@Data
public class LoanOfferResponse {
    private String offerId;
    private String userId;
    private Double offeredAmount;
    private Double interestRate;
    private Integer tenureMonths;
    private Double emiAmount;
    private String message;
}
