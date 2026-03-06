package com.loan.kyc_service.repository;

import com.loan.kyc_service.model.KycDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KycRepository extends MongoRepository<KycDetails, String> {
    KycDetails findByUserId(String userId);
}
