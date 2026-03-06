package com.loan.user_service.model;

import com.loan.user_service.dto.KycDetailsDto;
import com.loan.user_service.dto.LoanRequestDto;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Data
public class User {
    @Id
    private String customerId;
    private String name;
    private String email;
    private String phone;
    private String kycStatus;
    private Double annualIncome;
    private String employmentType;
    private KycDetailsDto kycDetailsDto;
    private LoanRequestDto loanRequestDto;
}
