package com.loan.loan_service.service;

import com.loan.loan_service.dto.*;
import com.loan.loan_service.model.Loan;
import com.loan.loan_service.model.LoanOffer;
import com.loan.loan_service.repository.LoanOfferRepository;
import com.loan.loan_service.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final LoanOfferRepository loanOfferRepository;
    private final RestTemplate restTemplate;

    @Value("${kyc.service.url}")
    private String kycServiceUrl;

    @Value("${user.service.url}")
    private String userServiceUrl;

    public ApplicationResponse generateLoanOffer(LoanRequest request) {
        log.info("=== LOAN REQUEST RECEIVED ===");
        log.info("UserId: {}", request.getUserId());
        log.info("RequestedAmount: {}", request.getRequestedAmount());
        log.info("TenureMonths: {}", request.getTenureMonths());
        String kycUrl = kycServiceUrl + "/kyc/status/" + request.getUserId();
        KycStatusResponse kycStatus;

        try {
            kycStatus = restTemplate.getForObject(kycUrl, KycStatusResponse.class);
        } catch (HttpClientErrorException e) {
            log.error("KYC service error: {}", e.getMessage());
            throw new RuntimeException("Could not verify KYC status. Please try again.");
        }
        if (kycStatus == null || !"VERIFIED".equals(kycStatus.getStatus())) {
            throw new RuntimeException("KYC not verified. Cannot generate loan offer.");
        }

        String userUrl = userServiceUrl + "/users/" + request.getUserId();
        User user;
        try {
            user = restTemplate.getForObject(userUrl, User.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Could not fetch user details.");
        }

        if (user == null || user.getAnnualIncome() == null) {
            throw new RuntimeException("User income details not found.");
        }

        double eligibleAmount = calculateEligibleAmount(user.getAnnualIncome(), user.getEmploymentType());
        double offeredAmount = Math.min(request.getRequestedAmount(), eligibleAmount);

        double interestRate = calculateInterestRate(user.getAnnualIncome(), request.getTenureMonths());
        double emi = calculateEMI(offeredAmount, interestRate, request.getTenureMonths());

        LoanOffer offer = new LoanOffer();
        offer.setUserId(request.getUserId());
        offer.setOfferedAmount(offeredAmount);
        offer.setInterestRate(interestRate);
        offer.setTenureMonths(request.getTenureMonths());
        offer.setEmiAmount(emi);
        offer.setStatus("PENDING");
        LoanOffer savedOffer = loanOfferRepository.save(offer);

        Loan loan = new Loan();
        loan.setUserId(request.getUserId());
        loan.setAmount(offeredAmount);
        loan.setTenureMonths(request.getTenureMonths());
        loan.setInterestRate(interestRate);
        loan.setStatus("OFFER_GENERATED");
        loan.setOfferId(savedOffer.getId());
        loan.setCreatedAt(LocalDateTime.now());
        Loan savedLoan = loanRepository.save(loan);

        ApplicationResponse response = new ApplicationResponse();
        response.setOfferId(savedOffer.getId());
        response.setUserId(request.getUserId());
        response.setLoanId(response.getLoanId());
        response.setOfferedAmount(offeredAmount);
        response.setInterestRate(interestRate);
        response.setTenureMonths(request.getTenureMonths());
        response.setEmiAmount(emi);
        response.setLoanStatus("OFFER_GENERATED");

        if (request.getRequestedAmount() > eligibleAmount) {
            response.setMessage(String.format(
                    "You requested ₹%.0f but based on your income you are eligible for ₹%.0f",
                    request.getRequestedAmount(), eligibleAmount
            ));
        } else {
            response.setMessage("Loan offer generated successfully!");
        }

        return response;
    }

    public Loan acceptLoanOffer(String loanId, String userId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan offer not found: " + loanId));

        if (!loan.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Loan does not belong to this user.");
        }

        if (loan.getOfferId() != null) {
            LoanOffer offer = loanOfferRepository.findById(loan.getOfferId()).orElse(null);
            if (offer != null) {
                offer.setStatus("ACCEPTED");
                loanOfferRepository.save(offer);
            }
        }

        loan.setStatus("ACCEPTED");
        loan.setUpdatedAt(LocalDateTime.now());
        loanRepository.save(loan);

        processDisbursement(loanId);
        return loanRepository.findById(loanId).get();
    }

    public Loan processDisbursement(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));

        if (!"ACCEPTED".equals(loan.getStatus())) {
            throw new RuntimeException("Loan must be accepted before disbursement.");
        }

        loan.setStatus("DISBURSEMENT_IN_PROGRESS");
        loan.setUpdatedAt(LocalDateTime.now());
        return loanRepository.save(loan);
    }

    public List<Loan> getLoansByUser(String userId) {
        return loanRepository.findByUserId(userId);
    }

    public List<LoanOffer> getLoanOffersByUser(String userId) {
        return loanOfferRepository.findByUserId(userId);
    }

    private double calculateEligibleAmount(Double annualIncome, String employmentType) {
        if ("SALARIED".equalsIgnoreCase(employmentType)) {
            return annualIncome * 3;
        } else if ("BUSINESS".equalsIgnoreCase(employmentType)) {
            return annualIncome * 2.5;
        } else {
            return annualIncome * 2; // SELF_EMPLOYED
        }
    }

    private double calculateInterestRate(Double annualIncome, Integer tenure) {
        double baseRate;
        if (annualIncome >= 1200000) {
            baseRate = 9.5;
        } else if (annualIncome >= 600000) {
            baseRate = 11.0;
        } else if (annualIncome >= 300000) {
            baseRate = 12.5;
        } else {
            baseRate = 14.0;
        }

        // Longer tenure → slight rate increase
        if (tenure > 36) baseRate += 0.5;

        return baseRate;
    }

    private double calculateEMI(Double principal, Double annualRate, Integer months) {
        double monthlyRate = annualRate / (12 * 100);
        double emi = (principal * monthlyRate * Math.pow(1 + monthlyRate, months))
                / (Math.pow(1 + monthlyRate, months) - 1);
        return Math.round(emi * 100.0) / 100.0;
    }
}

