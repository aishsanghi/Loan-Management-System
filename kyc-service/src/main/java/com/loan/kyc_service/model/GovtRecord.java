package com.loan.kyc_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "govt_records")
public class GovtRecord {
    @Id
    private String id;
    private String panNumber;
    private String aadhaarNumber;
}
