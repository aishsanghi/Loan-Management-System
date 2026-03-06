package com.loan.user_service.controller;

import com.loan.user_service.dto.CustomerRequest;
import com.loan.user_service.model.User;
import com.loan.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        User user = service.getUserById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}/kyc-status")
    public ResponseEntity<?> updateKycStatus(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {

        User updatedUser = service.updateKycStatus(userId, request.get("kycStatus"));
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyForLoan(@RequestBody CustomerRequest customerRequest) {
        return ResponseEntity.ok(service.processFullApplication(customerRequest));
    }
}
