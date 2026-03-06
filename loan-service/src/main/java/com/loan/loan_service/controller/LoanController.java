package com.loan.loan_service.controller;

import com.loan.loan_service.dto.ApplicationResponse;
import com.loan.loan_service.dto.LoanOfferResponse;
import com.loan.loan_service.dto.LoanRequest;
import com.loan.loan_service.model.Loan;
import com.loan.loan_service.model.LoanOffer;
import com.loan.loan_service.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/{userId}/offer")
    public ResponseEntity<ApplicationResponse> generateOffer(
            @PathVariable String userId,
            @RequestBody LoanRequest request) {
        request.setUserId(userId); // inject userId into request
        return ResponseEntity.ok(loanService.generateLoanOffer(request));
    }

    @PutMapping("/{loanId}/accept")
    public ResponseEntity<Loan> acceptOffer(
            @PathVariable String loanId,
            @RequestParam String userId) {
        return ResponseEntity.ok(loanService.acceptLoanOffer(loanId, userId));
    }

    @PutMapping("/{loanId}/disburse")
    public ResponseEntity<Loan> disburse(@PathVariable String loanId) {
        return ResponseEntity.ok(loanService.processDisbursement(loanId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Loan>> getLoansByUser(@PathVariable String userId) {
        return ResponseEntity.ok(loanService.getLoansByUser(userId));
    }

    @GetMapping("/offers/{userId}")
    public ResponseEntity<List<LoanOffer>> getLoanOffersByUser(@PathVariable String userId) {
        List<LoanOffer> offers = loanService.getLoanOffersByUser(userId);

        if (offers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .build();
        }

        return ResponseEntity.ok(offers);
    }
}
