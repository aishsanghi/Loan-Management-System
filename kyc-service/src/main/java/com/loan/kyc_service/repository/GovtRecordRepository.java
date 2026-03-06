package com.loan.kyc_service.repository;

import com.loan.kyc_service.model.GovtRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GovtRecordRepository extends MongoRepository<GovtRecord, String> {
    Optional<GovtRecord> findByPanNumberAndAadhaarNumber(
            String panNumber,
            String aadhaarNumber
    );
}
