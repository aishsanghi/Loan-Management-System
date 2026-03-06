package com.loan.loan_service.repository;


import com.loan.loan_service.model.Loan;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LoanRepository extends MongoRepository<Loan, String> {
    List<Loan> findByUserId(String userId);
}
