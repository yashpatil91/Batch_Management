package com.batchmanagement.backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.batchmanagement.backend.dto.common.ModuleResponse;
import com.batchmanagement.backend.entity.Batch;
import com.batchmanagement.backend.entity.Module;
import com.batchmanagement.backend.entity.ModuleTrainerHistory;
import com.batchmanagement.backend.entity.User;
import com.batchmanagement.backend.entity.enums.BatchStatus;
import com.batchmanagement.backend.entity.enums.Role;
import com.batchmanagement.backend.mapper.ModuleMapper;
import com.batchmanagement.backend.repository.BatchRepository;
import com.batchmanagement.backend.repository.BatchTopicRepository;
import com.batchmanagement.backend.repository.ModuleRepository;
import com.batchmanagement.backend.repository.ModuleTrainerHistoryRepository;
import com.batchmanagement.backend.repository.UserRepository;

@Service
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final BatchTopicRepository batchTopicRepository;
    private final ModuleTrainerHistoryRepository historyRepository;

    public ModuleServiceImpl(
            ModuleRepository moduleRepository,
            BatchRepository batchRepository,
            UserRepository userRepository,
            BatchTopicRepository batchTopicRepository,
            ModuleTrainerHistoryRepository historyRepository) {

        this.moduleRepository = moduleRepository;
        this.batchRepository = batchRepository;
        this.userRepository = userRepository;
        this.batchTopicRepository = batchTopicRepository;
        this.historyRepository = historyRepository;
    }

    // =========================
    // CREATE MODULE
    // =========================

    @Override
    public Module createModule(
            Module module,
            String requesterEmail) {

        System.out.println("[DEBUG][createModule] enter requester=" + requesterEmail
                + " batchId=" + (module.getBatch() == null ? "null" : module.getBatch().getId())
                + " trainerId=" + (module.getTrainer() == null ? "null" : module.getTrainer().getId())
                + " name=" + module.getName());

        User requester = userRepository
                .findByEmail(requesterEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User not found"
                        )
                );

        if (module.getBatch() == null ||
                module.getBatch().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Batch is required"
            );
        }

        Batch batch = batchRepository
                .findById(module.getBatch().getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Batch not found"
                        )
                );

        module.setBatch(batch);

        module.setProgress(0);

        module.setStatus("NOT_STARTED");

        // =========================
        // TRAINER SELF ASSIGN / ADMIN ASSIGN
        // =========================

        if (requester.getRole() == Role.TRAINER) {

            boolean participatesInBatch =
                    moduleRepository.findByBatchId(
                                    batch.getId()
                            )
                            .stream()
                            .anyMatch(existing ->
                                    existing.getTrainer() != null &&
                                            existing.getTrainer()
                                                    .getId()
                                                    .equals(
                                                            requester.getId()
                                                    )
                            );

            if (!participatesInBatch) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Trainer can add module only to assigned batch"
                );
            }

            module.setTrainer(requester);
        }

        if (module.getTrainer() != null) {

            System.out.println("[DEBUG][createModule] validating schedule trainerId=" + module.getTrainer().getId()
                    + " batchId=" + batch.getId());
            validateTrainerScheduleForBatch(
                    module.getTrainer().getId(),
                    batch.getId(),
                    null
            );
        }

        System.out.println("[DEBUG][createModule] saving module now.");
        Module savedModule =
                moduleRepository.save(module);

        updateBatchProgress(batch.getId());

        return savedModule;
    }

    // =========================
    // GET ALL MODULES
    // =========================

    @Override
    public List<Module> getAllModules() {

        return moduleRepository.findAll();
    }

    // =========================
    // GET MODULE BY ID
    // =========================

    @Override
    public Module getModuleById(Long id) {

        return moduleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Module not found"
                        )
                );
    }

    // =========================
    // GET MODULES BY BATCH
    // =========================

    @Override
    public List<Module> getModulesByBatch(Long batchId) {

        return moduleRepository.findByBatchId(batchId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleResponse> getModuleResponsesByBatch(Long batchId) {

        return moduleRepository
                .findByBatchIdFetchingAssociations(batchId)
                .stream()
                .map(ModuleMapper::toResponse)
                .toList();
    }

    // =========================
    // GET MODULES BY TRAINER
    // =========================

    @Override
    public List<Module> getModulesByTrainer(Long trainerId) {

        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Trainer not found"
                        )
                );

        return moduleRepository.findByTrainer(trainer);
    }

    // =========================
    // GET MODULES BY EMAIL
    // =========================

    @Override
    public List<Module> getModulesForTrainerEmail(
            String trainerEmail) {

        User trainer = userRepository
                .findByEmail(trainerEmail)
                .filter(user ->
                        user.getRole() == Role.TRAINER
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Trainer not found"
                        )
                );

        return moduleRepository.findByTrainer(trainer);
    }

    // =========================
    // ASSIGN / REASSIGN TRAINER
    // =========================

    @Override
    public Module assignTrainer(
            Long moduleId,
            Long trainerId,
            String requesterEmail) {

        System.out.println("[DEBUG][assignTrainer] enter moduleId=" + moduleId
                + " trainerId=" + trainerId + " requester=" + requesterEmail);

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Module not found"
                        )
                );

        validateModuleReassignmentAccess(
                module,
                requesterEmail
        );

        User newTrainer =
                userRepository.findById(trainerId)
                        .filter(user -> user.getRole() == Role.TRAINER)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Trainer not found"
                                )
                        );

        System.out.println("[DEBUG][assignTrainer] validate schedule newTrainerId=" + newTrainer.getId()
                + " batchId=" + (module.getBatch() == null ? "null" : module.getBatch().getId())
                + " excludeModuleId=" + module.getId());
        validateTrainerScheduleForBatch(
                newTrainer.getId(),
                module.getBatch().getId(),
                module.getId()
        );

        if (module.getTrainer() != null) {

            ModuleTrainerHistory history =
                    new ModuleTrainerHistory();

            history.setModule(module);

            history.setTrainer(
                    module.getTrainer()
            );

            history.setAssignedDate(
                    LocalDate.now()
            );

            history.setRemovedDate(
                    LocalDate.now()
            );

            historyRepository.save(history);
        }

        module.setTrainer(newTrainer);

        System.out.println("[DEBUG][assignTrainer] saving module now. moduleId=" + module.getId()
                + " newTrainerId=" + newTrainer.getId());
        Module updatedModule =
                moduleRepository.save(module);

        updateBatchProgress(
                module.getBatch().getId()
        );

        return updatedModule;
    }

    // =========================
    // SELF ASSIGN
    // =========================

    @Override
    public Module selfAssign(
            Long moduleId,
            String requesterEmail) {

        System.out.println("[DEBUG][selfAssign] enter moduleId=" + moduleId + " requester=" + requesterEmail);

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Module not found"
                        )
                );

        User trainer = userRepository
                .findByEmail(requesterEmail)
                .filter(user -> user.getRole() == Role.TRAINER)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Trainer not found"
                        )
                );

        System.out.println("[DEBUG][selfAssign] validate schedule trainerId=" + trainer.getId()
                + " batchId=" + (module.getBatch() == null ? "null" : module.getBatch().getId())
                + " excludeModuleId=" + module.getId());
        validateTrainerScheduleForBatch(
                trainer.getId(),
                module.getBatch().getId(),
                module.getId()
        );

        module.setTrainer(trainer);

        System.out.println("[DEBUG][selfAssign] saving module now. moduleId=" + module.getId()
                + " trainerId=" + trainer.getId());
        Module updatedModule =
                moduleRepository.save(module);

        updateBatchProgress(
                module.getBatch().getId()
        );

        return updatedModule;
    }

    // =========================
    // UPDATE STATUS
    // =========================

    @Override
    public Module updateStatus(
            Long moduleId,
            String status,
            String requesterEmail) {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Module not found"
                        )
                );

        validateModuleAccess(
                module,
                requesterEmail
        );

        module.setStatus(status);

        Module updatedModule =
                moduleRepository.save(module);

        updateBatchProgress(
                module.getBatch().getId()
        );

        return updatedModule;
    }

    // =========================
    // UPDATE DETAILS
    // =========================

    @Override
    public Module updateDetails(
            Long moduleId,
            String name,
            String status,
            String requesterEmail) {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Module not found"
                        )
                );

        validateModuleAccess(
                module,
                requesterEmail
        );

        if (name != null &&
                !name.trim().isEmpty()) {

            module.setName(name.trim());
        }

        if (status != null &&
                !status.trim().isEmpty()) {

            module.setStatus(
                    status.trim().toUpperCase()
            );
        }

        Module updatedModule =
                moduleRepository.save(module);

        updateBatchProgress(
                module.getBatch().getId()
        );

        return updatedModule;
    }

    // =========================
    // UPDATE MODULE PROGRESS
    // =========================

    @Override
    public void updateModuleProgress(Long moduleId) {

        Module module =
                moduleRepository.findById(moduleId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Module not found"
                                )
                        );

        long totalTopics =
                batchTopicRepository
                        .countByModule(module);

        long completedTopics =
                batchTopicRepository
                        .countByModuleAndCompletedTrue(module);

        int progress = 0;

        if (totalTopics > 0) {

            progress =
                    (int) (
                            (completedTopics * 100)
                                    / totalTopics
                    );
        }

        module.setProgress(progress);

        if (progress == 100) {

            module.setStatus("COMPLETED");

        } else if (progress > 0) {

            module.setStatus("ONGOING");

        } else {

            module.setStatus("NOT_STARTED");
        }

        moduleRepository.save(module);

        updateBatchProgress(
                module.getBatch().getId()
        );
    }

    // =========================
    // UPDATE BATCH PROGRESS
    // =========================

    @Override
    public void updateBatchProgress(Long batchId) {

        Batch batch =
                batchRepository.findById(batchId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Batch not found"
                                )
                        );

        List<Module> modules =
                moduleRepository.findByBatchId(batchId);

        if (modules.isEmpty()) {

            batch.setProgress(0);

            batch.setStatus(
                    BatchStatus.ONGOING
            );

            batchRepository.save(batch);

            return;
        }

        int totalProgress =
                modules.stream()
                        .mapToInt(module ->
                                module.getProgress() == null
                                        ? 0
                                        : module.getProgress()
                        )
                        .sum();

        int averageProgress =
                totalProgress / modules.size();

        batch.setProgress(averageProgress);

        boolean allCompleted =
                modules.stream()
                        .allMatch(module ->
                                "COMPLETED"
                                        .equalsIgnoreCase(
                                                module.getStatus()
                                        )
                        );

        if (allCompleted) {

            batch.setStatus(
                    BatchStatus.COMPLETED
            );

        } else {

            batch.setStatus(
                    BatchStatus.ONGOING
            );
        }

        batchRepository.save(batch);
    }

    // =========================
    // VALIDATE MANAGEMENT ACCESS
    // =========================

    @Override
    public void validateModuleManagementAccess(
            Long moduleId,
            String requesterEmail) {

        Module module =
                moduleRepository.findById(moduleId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Module not found"
                                )
                        );

        validateModuleAccess(
                module,
                requesterEmail
        );
    }

    // =========================
    // DELETE MODULE
    // =========================

    @Override
    public void deleteModule(
            Long moduleId,
            String requesterEmail) {

        Module module =
                moduleRepository.findById(moduleId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Module not found"
                                )
                        );

        validateModuleAccess(
                module,
                requesterEmail
        );

        Long batchId =
                module.getBatch() == null ? null : module.getBatch().getId();

        moduleRepository.delete(module);

        if (batchId != null) {
            updateBatchProgress(batchId);
        }
    }

    // =========================
    // VALIDATE MODULE ACCESS
    // =========================

    private void validateModuleAccess(
            Module module,
            String requesterEmail) {

        User requester =
                userRepository.findByEmail(requesterEmail)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "User not found"
                                )
                        );

        if (requester.getRole() == Role.ADMIN) {
            return;
        }

        if (requester.getRole() == Role.TRAINER &&
                module.getTrainer() != null &&
                module.getTrainer()
                        .getId()
                        .equals(requester.getId())) {

            return;
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Not authorized"
        );
    }

    // =========================
    // VALIDATE REASSIGN ACCESS
    // =========================

    private void validateModuleReassignmentAccess(
            Module module,
            String requesterEmail) {

        User requester =
                userRepository.findByEmail(requesterEmail)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "User not found"
                                )
                        );

        if (requester.getRole() == Role.ADMIN) {
            return;
        }

        if (requester.getRole() == Role.TRAINER) {

            boolean ownsModule =
                    module.getTrainer() != null &&
                            module.getTrainer()
                                    .getId()
                                    .equals(requester.getId());

            boolean participatesInBatch =
                    moduleRepository
                            .findByBatchId(
                                    module.getBatch().getId()
                            )
                            .stream()
                            .anyMatch(existing ->
                                    existing.getTrainer() != null &&
                                            existing.getTrainer()
                                                    .getId()
                                                    .equals(
                                                            requester.getId()
                                                    )
                            );

            if (ownsModule || participatesInBatch) {
                return;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Not authorized to reassign"
        );
    }

    @Override
    public void validateTrainerScheduleForBatch(
            Long trainerId,
            Long batchId,
            Long excludeModuleId) {

        System.out.println("[DEBUG][validateTrainerScheduleForBatch] enter trainerId=" + trainerId
                + " batchId=" + batchId + " excludeModuleId=" + excludeModuleId);

        User trainer = userRepository.findById(trainerId)
                .filter(user -> user.getRole() == Role.TRAINER)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Trainer not found"
                        )
                );

        Batch targetBatch = batchRepository.findById(batchId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Batch not found"
                        )
                );

        if (targetBatch.getStartDate() == null ||
                targetBatch.getEndDate() == null ||
                targetBatch.getTime() == null) {
            System.out.println("[DEBUG][validateTrainerScheduleForBatch] skip missing target fields startDate="
                    + targetBatch.getStartDate() + " endDate=" + targetBatch.getEndDate()
                    + " time=" + targetBatch.getTime());
            return;
        }

        System.out.println("[DEBUG][validateTrainerScheduleForBatch] target startDate=" + targetBatch.getStartDate()
                + " endDate=" + targetBatch.getEndDate()
                + " time=" + targetBatch.getTime()
                + " status=" + targetBatch.getStatus());

        List<Module> trainerModules =
                moduleRepository.findByTrainerFetchingBatch(trainer);

        System.out.println("[DEBUG][validateTrainerScheduleForBatch] trainerModules=" + trainerModules.size());

        for (Module existingModule : trainerModules) {

            if (excludeModuleId != null &&
                    excludeModuleId.equals(existingModule.getId())) {
                continue;
            }

            Batch existingBatch = existingModule.getBatch();

            if (existingBatch == null ||
                    existingBatch.getId() == null ||
                    existingBatch.getId().equals(targetBatch.getId()) ||
                    existingBatch.getStatus() == BatchStatus.COMPLETED ||
                    existingBatch.getStartDate() == null ||
                    existingBatch.getEndDate() == null ||
                    existingBatch.getTime() == null) {
                continue;
            }

            System.out.println("[DEBUG][validateTrainerScheduleForBatch] compare batchId=" + existingBatch.getId()
                    + " startDate=" + existingBatch.getStartDate()
                    + " endDate=" + existingBatch.getEndDate()
                    + " time=" + existingBatch.getTime()
                    + " status=" + existingBatch.getStatus()
                    + " moduleId=" + existingModule.getId());

            boolean dateOverlap =
                    !targetBatch.getStartDate().isAfter(existingBatch.getEndDate()) &&
                    !targetBatch.getEndDate().isBefore(existingBatch.getStartDate());

            if (!dateOverlap) {
                continue;
            }

            boolean timeOverlap = isTimeConflict(
                    targetBatch.getTime(),
                    existingBatch.getTime()
            );
            System.out.println("[DEBUG][validateTrainerScheduleForBatch] dateOverlap=true timeOverlap=" + timeOverlap);

            if (timeOverlap) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Trainer already has another module during this time."
                );
            }
        }
    }

    // =========================
    // TIME CONFLICT CHECK
    // =========================

    private boolean isTimeConflict(
            String time1,
            String time2) {

        String[] first =
                splitTimeRange(time1);

        String[] second =
                splitTimeRange(time2);

        if (first == null || second == null) {
            return false;
        }

        int start1 =
                convertToMinutes(first[0]);

        int end1 =
                convertToMinutes(first[1]);

        int start2 =
                convertToMinutes(second[0]);

        int end2 =
                convertToMinutes(second[1]);

        return start1 < end2 &&
                end1 > start2;
    }

    // =========================
    // SPLIT TIME RANGE
    // =========================

    private String[] splitTimeRange(
            String timeRange) {

        if (timeRange == null ||
                !timeRange.contains(" - ")) {

            return null;
        }

        return timeRange.split(" - ");
    }

    // =========================
    // CONVERT TIME TO MINUTES
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
                Integer.parseInt(parts[0]);

        int minute =
                Integer.parseInt(parts[1]);

        if (isPM && hour != 12) {
            hour += 12;
        }

        if (isAM && hour == 12) {
            hour = 0;
        }

        return (hour * 60) + minute;
    }
}