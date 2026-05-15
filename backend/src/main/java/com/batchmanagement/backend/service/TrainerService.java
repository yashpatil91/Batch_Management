package com.batchmanagement.backend.service;

import com.batchmanagement.backend.dto.common.BatchRequest;
import com.batchmanagement.backend.dto.common.BatchResponse;
import com.batchmanagement.backend.dto.trainer.ProgressUpdateRequest;
import com.batchmanagement.backend.entity.Batch;
import com.batchmanagement.backend.entity.BatchTopic;
import com.batchmanagement.backend.entity.Module;
import com.batchmanagement.backend.entity.User;
import com.batchmanagement.backend.entity.enums.BatchStatus;
import com.batchmanagement.backend.entity.enums.Role;
import com.batchmanagement.backend.exception.BadRequestException;
import com.batchmanagement.backend.exception.ResourceNotFoundException;
import com.batchmanagement.backend.mapper.BatchMapper;
import com.batchmanagement.backend.repository.BatchRepository;
import com.batchmanagement.backend.repository.BatchTopicRepository;
import com.batchmanagement.backend.repository.ModuleRepository;
import com.batchmanagement.backend.repository.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class TrainerService {

    private final UserRepository userRepository;
    private final BatchRepository batchRepository;
    private final BatchTopicRepository batchTopicRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleService moduleService;

    public TrainerService(
            UserRepository userRepository,
            BatchRepository batchRepository,
            BatchTopicRepository batchTopicRepository,
            ModuleRepository moduleRepository,
            ModuleService moduleService) {

        this.userRepository = userRepository;
        this.batchRepository = batchRepository;
        this.batchTopicRepository = batchTopicRepository;
        this.moduleRepository = moduleRepository;
        this.moduleService = moduleService;
    }

    // =========================
    // GET TRAINER BATCHES
    // =========================

    public List<BatchResponse> getAssignedBatches(
            String trainerEmail) {

        User trainer =
                findTrainerByEmail(trainerEmail);

        List<Module> trainerModules =
                moduleRepository.findByTrainer(trainer);

        Set<Batch> batches =
                trainerModules.stream()
                        .map(Module::getBatch)
                        .collect(Collectors.toSet());

        // IMPORTANT:
        // BATCH RESPONSE SHOULD CONTAIN
        // UPDATED COMBINED BATCH PROGRESS

        return batches.stream()
                .map(batch -> {

                    List<Module> batchModules =
                            moduleRepository.findByBatchId(
                                    batch.getId()
                            );

                    int avgProgress =
                            batchModules.isEmpty()
                                    ? 0
                                    : (int) Math.round(
                                            batchModules.stream()
                                                    .mapToInt(module ->
                                                            module.getProgress() == null
                                                                    ? 0
                                                                    : module.getProgress()
                                                    )
                                                    .average()
                                                    .orElse(0)
                                    );

                    boolean allModulesComplete =
                            !batchModules.isEmpty() &&
                                    batchModules.stream()
                                            .allMatch(module ->
                                                    module.getProgress() != null
                                                            && module.getProgress() >= 100
                                            );

                    BatchResponse response =
                            BatchMapper.toResponse(batch);

                    response.setProgress(avgProgress);

                    if (allModulesComplete) {

                        response.setStatus(
                                BatchStatus.COMPLETED
                        );

                    } else {

                        response.setStatus(
                                batch.getStatus() != null
                                        ? batch.getStatus()
                                        : BatchStatus.ONGOING
                        );
                    }

                    return response;

                })
                .toList();
    }

    // =========================
    // CREATE BATCH
    // =========================

    public BatchResponse createBatch(
            String trainerEmail,
            BatchRequest request) {

        User trainer =
                findTrainerByEmail(trainerEmail);

        if (request.getModuleName() == null ||
                request.getModuleName().trim().isEmpty()) {

            throw new BadRequestException(
                    "Starter module is required."
            );
        }

        // =========================
        // TRAINER CONFLICT CHECK
        // =========================

        List<Module> trainerModules =
                moduleRepository.findByTrainer(trainer);

        for (Module module : trainerModules) {

            Batch existing =
                    module.getBatch();

            if (existing == null ||
                    existing.getStatus() ==
                            BatchStatus.COMPLETED) {

                continue;
            }

            boolean dateOverlap =
                    !request.getStartDate()
                            .isAfter(existing.getEndDate()) &&
                    !request.getEndDate()
                            .isBefore(existing.getStartDate());

            if (!dateOverlap) continue;

            if (isTimeConflict(
                    request.getTime(),
                    existing.getTime())) {

                throw new BadRequestException(
                        "Trainer already has another module during this time."
                );
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
                    !request.getStartDate()
                            .isAfter(existing.getEndDate()) &&
                    !request.getEndDate()
                            .isBefore(existing.getStartDate());

            if (!dateOverlap) continue;

            if (isTimeConflict(
                    request.getTime(),
                    existing.getTime())) {

                throw new BadRequestException(
                        "Lab already occupied during this time."
                );
            }
        }

        // =========================
        // CREATE BATCH
        // =========================

        Batch batch = new Batch();

        batch.setDomainName(
                request.getDomainName()
        );

        batch.setStartDate(
                request.getStartDate()
        );

        batch.setEndDate(
                request.getEndDate()
        );

        batch.setProgress(0);

        batch.setStatus(
                BatchStatus.ONGOING
        );

        batch.setTime(
                request.getTime()
        );

        batch.setLabNo(
                request.getLabNo()
        );

        batch.setNoOfStudents(
                request.getNoOfStudents()
        );

        batch.setMeetLink(
                request.getMeetLink()
        );

        Batch savedBatch =
                batchRepository.save(batch);

        // =========================
        // CREATE STARTER MODULE
        // =========================

        Module module = new Module();

        module.setBatch(savedBatch);

        module.setName(
                request.getModuleName().trim()
        );

        module.setTrainer(trainer);

        module.setProgress(0);

        module.setStatus("NOT_STARTED");

        moduleRepository.save(module);

        return BatchMapper.toResponse(savedBatch);
    }

    // =========================
    // UPDATE PROGRESS
    // =========================
    // IMPORTANT:
    // DO NOT MANUALLY UPDATE
    // BATCH PROGRESS ANYMORE
    // =========================

    public BatchResponse updateProgress(
            String trainerEmail,
            Long batchId,
            ProgressUpdateRequest request) {

        Batch batch =
                findTrainerBatch(
                        trainerEmail,
                        batchId
                );

        return BatchMapper.toResponse(batch);
    }

    // =========================
    // COMPLETE BATCH
    // =========================
    // IMPORTANT:
    // BATCH COMPLETION NOW
    // DEPENDS ON ALL MODULES
    // =========================

    public BatchResponse markComplete(
            String trainerEmail,
            Long batchId) {

        findTrainerBatch(
                trainerEmail,
                batchId
        );

        List<Module> modules =
                moduleRepository.findByBatchId(batchId);

        for (Module module : modules) {

            moduleService.updateModuleProgress(
                    module.getId()
            );
        }

        List<Module> refreshed =
                moduleRepository.findByBatchId(batchId);

        if (refreshed.isEmpty()) {

            throw new BadRequestException(
                    "Batch has no modules"
            );
        }

        boolean allAt100 =
                refreshed.stream()
                        .allMatch(module ->
                                module.getProgress() != null
                                        && module.getProgress() >= 100
                        );

        if (!allAt100) {

            StringBuilder detail =
                    new StringBuilder(
                            "Cannot complete this batch until every module is at 100%. Still in progress: "
                    );

            refreshed.stream()
                    .filter(module ->
                            module.getProgress() == null
                                    || module.getProgress() < 100
                    )
                    .forEach(module ->
                            detail.append(module.getName())
                                    .append(" (")
                                    .append(
                                            module.getProgress() == null
                                                    ? 0
                                                    : module.getProgress()
                                    )
                                    .append("%), ")
                    );

            throw new BadRequestException(
                    detail.toString()
            );
        }

        Batch batch =
                batchRepository.findById(batchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Batch not found"
                                )
                        );

        batch.setProgress(100);

        batch.setStatus(
                BatchStatus.COMPLETED
        );

        return BatchMapper.toResponse(
                batchRepository.save(batch)
        );
    }

    // =========================
    // FIND TRAINER BATCH
    // =========================

    private Batch findTrainerBatch(
            String trainerEmail,
            Long batchId) {

        User trainer =
                findTrainerByEmail(trainerEmail);

        Batch batch =
                batchRepository.findById(batchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Batch not found"
                                )
                        );

        boolean moduleOwnerInBatch =
                moduleRepository.findByBatchId(batchId)
                        .stream()
                        .anyMatch(module ->
                                module.getTrainer() != null &&
                                        module.getTrainer()
                                                .getId()
                                                .equals(
                                                        trainer.getId()
                                                )
                        );

        if (!moduleOwnerInBatch) {

            throw new ResourceNotFoundException(
                    "Batch not assigned to this trainer"
            );
        }

        return batch;
    }

    // =========================
    // FIND TRAINER
    // =========================

    private User findTrainerByEmail(
            String email) {

        return userRepository.findByEmail(email)
                .filter(user ->
                        user.getRole() == Role.TRAINER
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Trainer not found"
                        )
                );
    }

    // =========================
    // UPDATE BATCH
    // =========================

    public BatchResponse updateBatch(
            String trainerEmail,
            Long batchId,
            BatchRequest request) {

        Batch batch =
                findTrainerBatch(
                        trainerEmail,
                        batchId
                );

        User trainer =
                findTrainerByEmail(trainerEmail);

        List<Module> trainerModules =
                moduleRepository.findByTrainer(trainer);

        for (Module module : trainerModules) {

            Batch existing =
                    module.getBatch();

            if (existing == null ||
                    existing.getId().equals(batchId) ||
                    existing.getStatus() ==
                            BatchStatus.COMPLETED) {

                continue;
            }

            boolean dateOverlap =
                    !request.getStartDate()
                            .isAfter(existing.getEndDate()) &&
                    !request.getEndDate()
                            .isBefore(existing.getStartDate());

            if (!dateOverlap) continue;

            if (isTimeConflict(
                    request.getTime(),
                    existing.getTime())) {

                throw new BadRequestException(
                        "Trainer already has another module during this time."
                );
            }
        }

        List<Batch> labBatches =
                batchRepository.findByLabNoAndStatusNot(
                        request.getLabNo(),
                        BatchStatus.COMPLETED
                );

        for (Batch existing : labBatches) {

            if (existing.getId().equals(batchId)) {
                continue;
            }

            boolean dateOverlap =
                    !request.getStartDate()
                            .isAfter(existing.getEndDate()) &&
                    !request.getEndDate()
                            .isBefore(existing.getStartDate());

            if (!dateOverlap) continue;

            if (isTimeConflict(
                    request.getTime(),
                    existing.getTime())) {

                throw new BadRequestException(
                        "Lab already occupied during this time."
                );
            }
        }

        batch.setDomainName(
                request.getDomainName()
        );

        batch.setStartDate(
                request.getStartDate()
        );

        batch.setEndDate(
                request.getEndDate()
        );

        batch.setTime(
                request.getTime()
        );

        batch.setLabNo(
                request.getLabNo()
        );

        batch.setNoOfStudents(
                request.getNoOfStudents()
        );

        batch.setMeetLink(
                request.getMeetLink()
        );

        Batch updated =
                batchRepository.save(batch);

        return BatchMapper.toResponse(updated);
    }

    // =========================
    // DELETE BATCH
    // =========================

    public void deleteBatch(
            String trainerEmail,
            Long batchId) {

        Batch batch =
                findTrainerBatch(
                        trainerEmail,
                        batchId
                );

        List<Module> modules =
                moduleRepository.findByBatchId(batchId);

        for (Module module : modules) {

            List<BatchTopic> topics =
                    batchTopicRepository.findByModule(module);

            batchTopicRepository.deleteAll(topics);
        }

        moduleRepository.deleteAll(modules);

        batchRepository.delete(batch);
    }

    // =========================
    // TIME CONFLICT
    // =========================

    private boolean isTimeConflict(
            String newTime,
            String existingTime) {

        if (newTime == null ||
                existingTime == null ||
                !newTime.contains("-") ||
                !existingTime.contains("-")) {

            return false;
        }

        String[] newParts =
                newTime.split("-");

        String[] existingParts =
                existingTime.split("-");

        if (newParts.length < 2 ||
                existingParts.length < 2) {

            return false;
        }

        int newStart =
                convertToMinutes(
                        newParts[0].trim()
                );

        int newEnd =
                convertToMinutes(
                        newParts[1].trim()
                );

        int oldStart =
                convertToMinutes(
                        existingParts[0].trim()
                );

        int oldEnd =
                convertToMinutes(
                        existingParts[1].trim()
                );

        return newStart < oldEnd &&
                newEnd > oldStart;
    }

    // =========================
    // CONVERT TIME
    // =========================

    private int convertToMinutes(
            String time) {

        time = time.trim().toUpperCase();

        boolean isPM =
                time.contains("PM");

        boolean isAM =
                time.contains("AM");

        time = time.replace("AM", "")
                .replace("PM", "")
                .trim();

        String[] parts =
                time.split(":");

        int hour =
                Integer.parseInt(
                        parts[0].trim()
                );

        int minute =
                Integer.parseInt(
                        parts[1].trim()
                );

        if (isPM && hour != 12) {
            hour += 12;
        }

        if (isAM && hour == 12) {
            hour = 0;
        }

        return hour * 60 + minute;
    }
}