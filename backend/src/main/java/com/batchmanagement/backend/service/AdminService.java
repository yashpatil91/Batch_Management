package com.batchmanagement.backend.service;

import com.batchmanagement.backend.dto.admin.AssignBatchRequest;
import com.batchmanagement.backend.dto.admin.CreateBatchRequest;
import com.batchmanagement.backend.dto.admin.DashboardResponse;
import com.batchmanagement.backend.dto.common.BatchResponse;
import com.batchmanagement.backend.dto.common.BatchWithModulesResponse;
import com.batchmanagement.backend.dto.common.UserCreateRequest;
import com.batchmanagement.backend.dto.common.UserResponse;
import com.batchmanagement.backend.dto.common.UserUpdateRequest;
import com.batchmanagement.backend.entity.Batch;
import com.batchmanagement.backend.entity.Module;
import com.batchmanagement.backend.entity.User;
import com.batchmanagement.backend.entity.enums.BatchStatus;
import com.batchmanagement.backend.entity.enums.Role;
import com.batchmanagement.backend.exception.BadRequestException;
import com.batchmanagement.backend.exception.ResourceNotFoundException;
import com.batchmanagement.backend.mapper.BatchMapper;
import com.batchmanagement.backend.mapper.ModuleMapper;
import com.batchmanagement.backend.mapper.UserMapper;
import com.batchmanagement.backend.repository.BatchRepository;
import com.batchmanagement.backend.repository.ModuleRepository;
import com.batchmanagement.backend.repository.UserRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final BatchRepository batchRepository;
    private final ModuleRepository moduleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public AdminService(UserRepository userRepository,
                        BatchRepository batchRepository,
                        ModuleRepository moduleRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.batchRepository = batchRepository;
        this.moduleRepository = moduleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ================= TRAINER =================

    public List<UserResponse> getTrainers() {
        List<User> trainers = userRepository.findByRole(Role.TRAINER);
        return trainers.stream()
                .map(trainer -> {
                	int totalBatches =
                		    batchRepository.countByTrainerAndStatusNot(
                		        trainer,
                		        BatchStatus.COMPLETED
                		    );
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

    public List<BatchWithModulesResponse> getAllBatchesWithModules() {
        return batchRepository.findAll()
                .stream()
                .map(batch -> {
                    BatchWithModulesResponse response = new BatchWithModulesResponse();
                    response.setId(batch.getId());
                    response.setDomainName(batch.getDomainName());
                    response.setStartDate(batch.getStartDate());
                    response.setEndDate(batch.getEndDate());
                    response.setProgress(batch.getProgress());
                    response.setStatus(batch.getStatus());
                    response.setTime(batch.getTime());
                    response.setLabNo(batch.getLabNo());
                    response.setNoOfStudents(batch.getNoOfStudents());
                    response.setMeetLink(batch.getMeetLink());
                    response.setModules(moduleRepository.findByBatch(batch)
                            .stream()
                            .map(ModuleMapper::toResponse)
                            .toList());
                    return response;
                })
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
        moduleRepository.findByBatch(savedBatch).forEach(module -> {
            if (module.getTrainer() == null) {
                module.setTrainer(trainer);
                moduleRepository.save(module);
            }
        });

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

    private void createModulesForBatch(Batch batch, List<CreateBatchRequest.ModuleRequest> modules, User defaultTrainer) {
        if (modules == null || modules.isEmpty()) {
            return;
        }
        for (CreateBatchRequest.ModuleRequest moduleRequest : modules) {
            if (moduleRequest.getName() == null || moduleRequest.getName().trim().isEmpty()) {
                continue;
            }
            Module module = new Module();
            module.setBatch(batch);
            module.setName(moduleRequest.getName().trim());
            module.setProgress(0);
            module.setStatus("NOT_STARTED");

            if (moduleRequest.getTrainerId() != null) {
                User trainer = userRepository.findById(moduleRequest.getTrainerId())
                        .filter(user -> user.getRole() == Role.TRAINER)
                        .orElseThrow(() -> new ResourceNotFoundException("Trainer not found for module"));
                module.setTrainer(trainer);
            } else if (defaultTrainer != null) {
                module.setTrainer(defaultTrainer);
            }
            moduleRepository.save(module);
        }
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
        batch.setMeetLink(request.getMeetLink());

        User trainer = null;

        if (request.getTrainerId() != null) {
            trainer = userRepository.findById(request.getTrainerId())
                    .filter(user -> user.getRole() == Role.TRAINER)
                    .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));

            batch.setTrainer(trainer);
        }

        // =========================
        // TRAINER CONFLICT CHECK
        // =========================
        if (trainer != null) {

            List<Batch> activeBatches =
                batchRepository.findByTrainerAndStatusNot(
                    trainer,
                    BatchStatus.COMPLETED
                );

            for (Batch existing : activeBatches) {

                boolean dateOverlap =
                    !request.getStartDate().isAfter(existing.getEndDate()) &&
                    !request.getEndDate().isBefore(existing.getStartDate());

                if (!dateOverlap) continue;

                if (isTimeConflict(request.getTime(), existing.getTime())) {
                    throw new BadRequestException(
                        "Trainer already has another active batch during this time."
                    );
                }
            }
        }

        // =========================
        // LAB CONFLICT CHECK
        // =========================
        List<Batch> labBatches =
            batchRepository.findByLabNoAndStatusNot(
                request.getLabNo(),
                BatchStatus.COMPLETED
            );

        for (Batch existing : labBatches) {

            boolean dateOverlap =
                !request.getStartDate().isAfter(existing.getEndDate()) &&
                !request.getEndDate().isBefore(existing.getStartDate());

            if (!dateOverlap) continue;

            if (isTimeConflict(request.getTime(), existing.getTime())) {
                throw new BadRequestException(
                    "Lab already occupied during this time."
                );
            }
        }

        // =========================
        // SAVE ONLY AFTER VALIDATION
        // =========================
        Batch savedBatch = batchRepository.save(batch);
        createModulesForBatch(savedBatch, request.getModules(), trainer);

        // =========================
        // EMAIL
        // =========================
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

        // Get assigned trainer
        User trainer = batch.getTrainer();

        // =========================
        // LAB CONFLICT CHECK
        // =========================
        List<Batch> labBatches =
            batchRepository.findByLabNoAndStatusNot(
                request.getLabNo(),
                BatchStatus.COMPLETED
            );

        for (Batch existing : labBatches) {

            // Skip current batch
            if (existing.getId().equals(batch.getId())) continue;

            boolean dateOverlap =
                !request.getStartDate().isAfter(existing.getEndDate()) &&
                !request.getEndDate().isBefore(existing.getStartDate());

            if (!dateOverlap) continue;

            if (isTimeConflict(request.getTime(), existing.getTime())) {
                throw new BadRequestException(
                    "Lab already occupied during this time."
                );
            }
        }

        // =========================
        // TRAINER CONFLICT CHECK
        // =========================
        if (trainer != null) {

            List<Batch> activeBatches =
                batchRepository.findByTrainerAndStatusNot(
                    trainer,
                    BatchStatus.COMPLETED
                );

            for (Batch existing : activeBatches) {

                // Skip current batch
                if (existing.getId().equals(batch.getId())) continue;

                boolean dateOverlap =
                    !request.getStartDate().isAfter(existing.getEndDate()) &&
                    !request.getEndDate().isBefore(existing.getStartDate());

                if (!dateOverlap) continue;

                if (isTimeConflict(request.getTime(), existing.getTime())) {
                    throw new BadRequestException(
                        "Trainer already has another active batch during this time."
                    );
                }
            }
        }

        // =========================
        // UPDATE FIELDS
        // =========================
        batch.setDomainName(request.getDomainName());
        batch.setStartDate(request.getStartDate());
        batch.setEndDate(request.getEndDate());
        batch.setTime(request.getTime());
        batch.setLabNo(request.getLabNo());
        batch.setNoOfStudents(request.getNoOfStudents());
        batch.setMeetLink(request.getMeetLink());
        

        Batch updated = batchRepository.save(batch);

        return BatchMapper.toResponse(updated);
    }
    private boolean isTimeConflict(String newTime, String existingTime) {

        String[] newParts = newTime.split("-");
        String[] oldParts = existingTime.split("-");

        int newStart = convertToMinutes(newParts[0].trim());
        int newEnd   = convertToMinutes(newParts[1].trim());

        int oldStart = convertToMinutes(oldParts[0].trim());
        int oldEnd   = convertToMinutes(oldParts[1].trim());

        return newStart < oldEnd && newEnd > oldStart;
    }

    private int convertToMinutes(String time) {
        time = time.trim().toUpperCase();

        boolean isPM = time.contains("PM");
        boolean isAM = time.contains("AM");

        time = time.replace("AM", "").replace("PM", "").trim();

        String[] parts = time.split(":");

        int hour   = Integer.parseInt(parts[0].trim());
        int minute = Integer.parseInt(parts[1].trim()); // ✅ parts[1] is now clean "00", not "00 - 17"

        if (isPM && hour != 12) hour += 12;
        if (isAM && hour == 12) hour = 0;

        return hour * 60 + minute;
    }
}
