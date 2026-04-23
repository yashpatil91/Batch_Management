package com.batchmanagement.backend.controller;

import com.batchmanagement.backend.dto.admin.AssignBatchRequest;
import com.batchmanagement.backend.dto.admin.CreateBatchRequest;
import com.batchmanagement.backend.dto.admin.DashboardResponse;
import com.batchmanagement.backend.dto.common.BatchResponse;
import com.batchmanagement.backend.dto.common.UserCreateRequest;
import com.batchmanagement.backend.dto.common.UserResponse;
import com.batchmanagement.backend.dto.common.UserUpdateRequest;
import com.batchmanagement.backend.service.AdminService;
import com.batchmanagement.backend.service.EmailService;

import jakarta.validation.Valid;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/trainers")
    public ResponseEntity<List<UserResponse>> getTrainers() {
        return ResponseEntity.ok(adminService.getTrainers());
    }

    @PostMapping("/trainers")
    public ResponseEntity<UserResponse> createTrainer(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(adminService.createTrainer(request));
    }

    @PutMapping("/trainers/{id}")
    public ResponseEntity<UserResponse> updateTrainer(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateTrainer(id, request));
    }

    @DeleteMapping("/trainers/{id}")
    public ResponseEntity<Void> deleteTrainer(@PathVariable Long id) {
        adminService.deleteTrainer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admins")
    public ResponseEntity<List<UserResponse>> getAdmins() {
        return ResponseEntity.ok(adminService.getAdmins());
    }

    @PostMapping("/admins")
    public ResponseEntity<UserResponse> createAdmin(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(adminService.createAdmin(request));
    }

    @GetMapping("/batches")
    public ResponseEntity<List<BatchResponse>> getBatches() {
        return ResponseEntity.ok(adminService.getAllBatches());
    }

    @PostMapping("/assign-batch")
    public ResponseEntity<BatchResponse> assignBatch(@Valid @RequestBody AssignBatchRequest request) {
        return ResponseEntity.ok(adminService.assignBatch(request));
    }

    @PostMapping("/batches")
    public ResponseEntity<BatchResponse> createBatch(@Valid @RequestBody CreateBatchRequest request) {
        return ResponseEntity.ok(adminService.createBatch(request));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }
    @Autowired
    private EmailService emailService;

    @GetMapping("/test-email")
    public String testEmail() {

        emailService.sendEmail(
            "yourgmail@gmail.com",   // 👈 YOUR EMAIL HERE
            "Test Email",
            "Spring Boot Email Working"
        );

        return "Email Sent";
    }
}
