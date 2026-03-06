package com.loan.loan_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "loan_offers")
public class LoanOffer {
    @Id
    private String id;
    private String userId;
    private Double offeredAmount;
    private Double interestRate;
    private Integer tenureMonths;
    private Double emiAmount;
    private String status;
}
