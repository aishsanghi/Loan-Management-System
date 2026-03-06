package com.loan.kyc_service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "kyc")
public class KycDetails {

    @Id
    private String id;

    private String userId;
    @NotBlank(message = "PAN Number is required")
    @Pattern(
            regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
            message = "Invalid PAN format"
    )
    private String panNumber;

    @NotBlank(message = "Aadhaar Number is required")
    @Pattern(
            regexp = "^[0-9]{12}$",
            message = "Aadhaar must be 12 digits"
    )
    private String aadhaarNumber;

    private Boolean panVerified;
    private Boolean aadhaarVerified;
    private String verificationStatus;
}
