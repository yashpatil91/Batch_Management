package com.batchmanagement.backend.service;

import com.batchmanagement.backend.dto.admin.AssignBatchRequest;
import com.batchmanagement.backend.dto.admin.CreateBatchRequest;
import com.batchmanagement.backend.dto.admin.DashboardResponse;
import com.batchmanagement.backend.dto.common.BatchResponse;
import com.batchmanagement.backend.dto.common.UserCreateRequest;
import com.batchmanagement.backend.dto.common.UserResponse;
import com.batchmanagement.backend.dto.common.UserUpdateRequest;
import com.batchmanagement.backend.entity.Batch;
import com.batchmanagement.backend.entity.User;
import com.batchmanagement.backend.entity.enums.BatchStatus;
import com.batchmanagement.backend.entity.enums.Role;
import com.batchmanagement.backend.exception.BadRequestException;
import com.batchmanagement.backend.exception.ResourceNotFoundException;
import com.batchmanagement.backend.mapper.BatchMapper;
import com.batchmanagement.backend.mapper.UserMapper;
import com.batchmanagement.backend.repository.BatchRepository;
import com.batchmanagement.backend.repository.UserRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final BatchRepository batchRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public AdminService(UserRepository userRepository,
                        BatchRepository batchRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.batchRepository = batchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ================= TRAINER =================

    public List<UserResponse> getTrainers() {
        List<User> trainers = userRepository.findByRole(Role.TRAINER);
        return trainers.stream()
                .map(trainer -> {
                    int totalBatches = batchRepository.countByTrainer(trainer);
                    return UserMapper.toResponse(trainer, totalBatches);
                })
                .toList();
    }

    public UserResponse createTrainer(UserCreateRequest request) {
        return UserMapper.toResponse(createUser(request, Role.TRAINER));
    }

    public UserResponse updateTrainer(Long id, UserUpdateRequest request) {
        User trainer = userRepository.findById(id)
                .filter(user -> user.getRole() == Role.TRAINER)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));

        if (!trainer.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        trainer.setName(request.getName());
        trainer.setEmail(request.getEmail());
        trainer.setExpertise(request.getExpertise());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            trainer.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return UserMapper.toResponse(userRepository.save(trainer));
    }

    public void deleteTrainer(Long id) {
        User trainer = userRepository.findById(id)
                .filter(user -> user.getRole() == Role.TRAINER)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));

        userRepository.delete(trainer);
    }

    // ================= ADMIN =================

    public List<UserResponse> getAdmins() {
        return userRepository.findByRole(Role.ADMIN)
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    public UserResponse createAdmin(UserCreateRequest request) {
        return UserMapper.toResponse(createUser(request, Role.ADMIN));
    }

    // ================= BATCH =================

    public List<BatchResponse> getAllBatches() {
        return batchRepository.findAll()
                .stream()
                .map(BatchMapper::toResponse)
                .toList();
    }

    // 🔥 ASSIGN BATCH + HTML EMAIL
    public BatchResponse assignBatch(AssignBatchRequest request) {

        Batch batch = batchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        User trainer = userRepository.findById(request.getTrainerId())
                .filter(user -> user.getRole() == Role.TRAINER)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));

        batch.setTrainer(trainer);

        Batch savedBatch = batchRepository.save(batch);

        // 🔥 HTML EMAIL
        try {
            emailService.sendBatchEmail(
                    trainer.getEmail(),
                    trainer.getName(),
                    savedBatch.getDomainName(),
                    savedBatch.getStartDate().toString()
            );
        } catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }

        return BatchMapper.toResponse(savedBatch);
    }

    // 🔥 CREATE BATCH + HTML EMAIL
    public BatchResponse createBatch(CreateBatchRequest request) {

        Batch batch = new Batch();
        batch.setDomainName(request.getDomainName());
        batch.setStartDate(request.getStartDate());
        batch.setEndDate(request.getEndDate());
        batch.setTime(request.getTime());
        batch.setLabNo(request.getLabNo());
        batch.setNoOfStudents(request.getNoOfStudents());
        batch.setProgress(request.getProgress() == null ? 0 : request.getProgress());
        batch.setStatus(BatchStatus.ONGOING);

        User trainer = null;

        if (request.getTrainerId() != null) {
            trainer = userRepository.findById(request.getTrainerId())
                    .filter(user -> user.getRole() == Role.TRAINER)
                    .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));

            batch.setTrainer(trainer);
        }

        Batch savedBatch = batchRepository.save(batch);

        // 🔥 EMAIL IF TRAINER EXISTS
        if (trainer != null) {
            try {
                emailService.sendBatchEmail(
                        trainer.getEmail(),
                        trainer.getName(),
                        savedBatch.getDomainName(),
                        savedBatch.getStartDate().toString()
                );
            } catch (Exception e) {
                System.out.println("Email failed: " + e.getMessage());
            }
        }

        return BatchMapper.toResponse(savedBatch);
    }

    // ================= DASHBOARD =================

    public DashboardResponse getDashboard() {
        DashboardResponse response = new DashboardResponse();

        response.setTotalTrainers(userRepository.findByRole(Role.TRAINER).size());
        response.setTotalBatches(batchRepository.count());
        response.setOngoingBatches(batchRepository.countByStatus(BatchStatus.ONGOING));

        return response;
    }

    // ================= COMMON =================

    private User createUser(UserCreateRequest request, Role role) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setExpertise(request.getExpertise());

        return userRepository.save(user);
    }
    
    //delete batch
    public void deleteBatch(Long id) {
        Batch batch = batchRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        // 🔥 VERY IMPORTANT (fixes your error)
        batch.setTrainer(null);

        batchRepository.delete(batch);
    }
    
    //edit
    public BatchResponse updateBatch(Long id, CreateBatchRequest request) {

        Batch batch = batchRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        // ✅ Update fields (same as trainer)
        batch.setDomainName(request.getDomainName());
        batch.setStartDate(request.getStartDate());
        batch.setEndDate(request.getEndDate());
        batch.setTime(request.getTime());
        batch.setLabNo(request.getLabNo());
        batch.setNoOfStudents(request.getNoOfStudents());

        Batch updated = batchRepository.save(batch);

        return BatchMapper.toResponse(updated);
    }
}