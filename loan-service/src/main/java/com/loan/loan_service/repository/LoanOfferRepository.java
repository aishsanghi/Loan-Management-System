package com.loan.loan_service.repository;

import com.loan.loan_service.model.LoanOffer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LoanOfferRepository extends MongoRepository<LoanOffer, String> {
    List<LoanOffer> findByUserId(String userId);
}
