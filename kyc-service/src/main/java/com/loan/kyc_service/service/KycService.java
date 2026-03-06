package com.loan.kyc_service.service;

import com.loan.kyc_service.dto.ApplicationResponse;
import com.loan.kyc_service.dto.KycServiceRequest;
import com.loan.kyc_service.dto.LoanServiceRequest;
import com.loan.kyc_service.model.*;
import com.loan.kyc_service.repository.GovtRecordRepository;
import com.loan.kyc_service.repository.KycRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class KycService {
    @Autowired
    private KycRepository kycRepository;

    @Autowired
    private GovtRecordRepository govtRecordRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${user-service.url}")
    private String userServiceUrl;

    @Value("${loan.service.url}")
    private String loanServiceUrl;

    public KycDetails getKycByUserId(String userId) {
        return kycRepository.findByUserId(userId); // adjust based on your repo method
    }

    @Async
    private void updateUserKycStatus(String userId, String kycStatus) {
        try{
            String url = userServiceUrl + "/users/" + userId + "/kyc-status";

            Map<String, String> request = new HashMap<>();
            request.put("kycStatus", kycStatus);
            restTemplate.put(url, request);
            System.out.println("Updated kycStatus to " + kycStatus + " for user: " + userId);
        } catch (Exception e) {
            System.err.println("Failed to update kycStatus for user " + userId + ": " + e.getMessage());
        }
    }


    public ApplicationResponse submitAndProcess(String userId, KycServiceRequest request) {
//        log.info("===  submit RECEIVED ===");
//        log.info("UserId: {}", request.getUserId());
//        log.info("RequestedAmount: {}", request.getRequestedAmount());
//        log.info("TenureMonths: {}", request.getTenureMonths());
        KycDetails existingKyc = kycRepository.findByUserId(userId);
        if (existingKyc != null && "VERIFIED".equals(existingKyc.getVerificationStatus())) {
            log.info("KYC already verified for user: {}, proceeding to loan",userId);
            return callLoanService(userId, request);
        }
        KycDetails kyc;
        if (existingKyc != null && "REJECTED".equals(existingKyc.getVerificationStatus())) {
            existingKyc.setPanNumber(request.getPanNumber());
            existingKyc.setAadhaarNumber(request.getAadhaarNumber());
            existingKyc.setVerificationStatus("PENDING");
            kyc = kycRepository.save(existingKyc);
        }else {
            // Fresh submission
            kyc = new KycDetails();
            kyc.setUserId(userId);
            kyc.setAadhaarNumber(request.getAadhaarNumber());
            kyc.setPanNumber(request.getPanNumber());
            kyc.setVerificationStatus("PENDING");
            kyc = kycRepository.save(kyc);
        }
        String verificationStatus = verifySynchronously(kyc);

        if (!"VERIFIED".equals(verificationStatus)) {
            throw new RuntimeException(
                    "KYC verification failed. Aadhaar/PAN not found in govt records."
            );
        }

        log.info("KYC verified for user: {}, calling Loan Service", userId);
        return callLoanService(userId, request);
    }

    private String verifySynchronously(KycDetails kycDetails) {
        Optional<GovtRecord> record = govtRecordRepository
                .findByPanNumberAndAadhaarNumber(
                        kycDetails.getPanNumber(),
                        kycDetails.getAadhaarNumber()
                );

        String verificationStatus;
        if (record.isPresent()) {
            kycDetails.setAadhaarVerified(true);
            kycDetails.setPanVerified(true);
            verificationStatus = "VERIFIED";
        } else {
            kycDetails.setAadhaarVerified(false);
            kycDetails.setPanVerified(false);
            verificationStatus = "REJECTED";
        }

        kycDetails.setVerificationStatus(verificationStatus);
        kycRepository.save(kycDetails);
        updateUserKycStatus(kycDetails.getUserId(), verificationStatus);

        return verificationStatus;
    }

    private ApplicationResponse callLoanService(String userId, KycServiceRequest request) {

        LoanServiceRequest loanRequest = new LoanServiceRequest();
        loanRequest.setUserId(userId);
        loanRequest.setRequestedAmount(request.getRequestedAmount());
        loanRequest.setTenureMonths(request.getTenureMonths());

        String loanUrl = loanServiceUrl + "/api/loans/" + userId + "/offer";

        try {
            ApplicationResponse response = restTemplate.postForObject(
                    loanUrl, loanRequest, ApplicationResponse.class
            );
            if (response != null) {
                response.setKycStatus("VERIFIED");
            }
            return response;
        } catch (Exception e) {
            log.error("Loan service call failed: {}", e.getMessage());
            throw new RuntimeException("Application failed at Loan step: " + e.getMessage());
        }
    }
}

