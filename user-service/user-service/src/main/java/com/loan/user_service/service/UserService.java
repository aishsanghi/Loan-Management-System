package com.loan.user_service.service;

import com.loan.user_service.dto.ApplicationResponse;
import com.loan.user_service.dto.CustomerRequest;
import com.loan.user_service.dto.KycServiceRequest;
import com.loan.user_service.model.User;
import com.loan.user_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class UserService {
    @Autowired
    private final UserRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${kyc.service.url}")
    private String kycServiceUrl;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User getUserById(String userId) {
        return repository.findById(userId).orElse(null);
    }

    public User updateKycStatus(String userId, String kycStatus){
        User user=repository.findById(userId)
                .orElseThrow(()->new RuntimeException("User not found with this id:"+userId));
        user.setKycStatus(kycStatus);
        return repository.save(user);
    }


    public ApplicationResponse processFullApplication(CustomerRequest request) {

        User existingUser = repository.findByEmail(request.getEmail());
        if (existingUser != null) {
            throw new RuntimeException("User already registered with email: " + request.getEmail());
        }

        User existingByPhone = repository.findByPhone(request.getPhone());
        if (existingByPhone != null) {
            throw new RuntimeException("User already registered with phone: " + request.getPhone());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAnnualIncome(request.getAnnualIncome());
        user.setEmploymentType(request.getEmploymentType());
        User savedUser = repository.save(user);

        KycServiceRequest kycRequest = new KycServiceRequest();
        kycRequest.setUserId(savedUser.getCustomerId());
        kycRequest.setAadhaarNumber(request.getKycDetails().getAadhaarNumber());
        kycRequest.setPanNumber(request.getKycDetails().getPanNumber());
        kycRequest.setRequestedAmount(request.getLoanRequest().getRequestedAmount());
        kycRequest.setTenureMonths(request.getLoanRequest().getTenureMonths());
        log.info("Sending to KYC: amount={}, tenure={}",
                kycRequest.getRequestedAmount(),
                kycRequest.getTenureMonths());

        String kycUrl = kycServiceUrl + "/kyc/submit-and-process/" + savedUser.getCustomerId();
        try {
            return restTemplate.postForObject(kycUrl, kycRequest, ApplicationResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Application failed at KYC step: " + e.getMessage());
        }
    }
}
