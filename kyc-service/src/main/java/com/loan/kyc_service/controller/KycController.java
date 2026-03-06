package com.loan.kyc_service.controller;

import com.loan.kyc_service.dto.KycServiceRequest;
import com.loan.kyc_service.model.KycDetails;
import com.loan.kyc_service.service.KycService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/kyc")
public class KycController {

    @Autowired
    private KycService service;

    @GetMapping("/status/{userId}")
    public ResponseEntity<?> getKycStatus(@PathVariable String userId) {
        KycDetails kyc = service.getKycByUserId(userId);

        if (kyc == null) {
            return ResponseEntity.status(404).body("KYC not found for user: " + userId);
        }

        Map<String, String> response = new HashMap<>();
        response.put("userId", userId);
        response.put("status", kyc.getVerificationStatus()); // make sure KycDetails has a status field
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit-and-process/{userId}")
    public ResponseEntity<?> submitAndProcess(
            @PathVariable String userId,
            @RequestBody KycServiceRequest request) {
        log.info("=== KYC CONTROLLER HIT ===");
        log.info("Path userId: {}", userId);
        log.info("Body requestedAmount: {}", request.getRequestedAmount());
        log.info("Body tenureMonths: {}", request.getTenureMonths());
        return ResponseEntity.ok(service.submitAndProcess(userId, request));
    }

}
