package com.loan.loan_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "loans")
public class Loan {
    @Id
    private String id;
    private String userId;
    private String offerId;
    private Double amount;
    private Integer tenureMonths;
    private Double interestRate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
