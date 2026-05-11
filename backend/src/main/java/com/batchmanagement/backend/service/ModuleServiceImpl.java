package com.batchmanagement.backend.service;

import com.batchmanagement.backend.entity.Batch;
import com.batchmanagement.backend.entity.Module;
import com.batchmanagement.backend.entity.ModuleTrainerHistory;
import com.batchmanagement.backend.entity.User;
import com.batchmanagement.backend.entity.enums.BatchStatus;
import com.batchmanagement.backend.entity.enums.Role;

import com.batchmanagement.backend.repository.BatchRepository;
import com.batchmanagement.backend.repository.BatchTopicRepository;
import com.batchmanagement.backend.repository.ModuleRepository;
import com.batchmanagement.backend.repository.ModuleTrainerHistoryRepository;
import com.batchmanagement.backend.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

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
        // TRAINER SELF ASSIGN
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

        Module savedModule =
                moduleRepository.save(module);

        // IMPORTANT
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
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Trainer not found"
                                )
                        );

        // SAVE OLD TRAINER HISTORY

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

        // ASSIGN NEW TRAINER

        module.setTrainer(newTrainer);

        Module updatedModule =
                moduleRepository.save(module);

        // IMPORTANT
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

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Module not found"
                        )
                );

        User trainer = userRepository
                .findByEmail(requesterEmail)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Trainer not found"
                        )
                );

        module.setTrainer(trainer);

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

        // AUTO STATUS

        if (progress == 100) {

            module.setStatus("COMPLETED");

        } else if (progress > 0) {

            module.setStatus("ONGOING");

        } else {

            module.setStatus("NOT_STARTED");
        }

        moduleRepository.save(module);

        // IMPORTANT
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

        // NO MODULES

        if (modules.isEmpty()) {

            batch.setProgress(0);

            batch.setStatus(
                    BatchStatus.ONGOING
            );

            batchRepository.save(batch);

            return;
        }

        // IMPORTANT
        // AVERAGE OF ALL MODULES

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

        // COMPLETE ONLY IF ALL DONE

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
    // VALIDATE MODULE ACCESS
    // =========================

    private void validateModuleAccess(
            Module module,
            String requesterEmail) {

        User requester =
                userRepository.findByEmail(requesterEmail)
                        .orElseThrow(() ->
                                new RuntimeException(
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
                                new RuntimeException(
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
}