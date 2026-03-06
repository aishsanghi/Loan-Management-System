package com.loan.user_service.dto;
import lombok.Data;

@Data
public class LoanRequestDto {
    private Double requestedAmount;
    private Integer tenureMonths;
}
