package com.batchmanagement.backend.service;

import com.batchmanagement.backend.dto.common.ModuleResponse;
import com.batchmanagement.backend.entity.Module;

import java.util.List;

public interface ModuleService {

    // =========================
    // CREATE MODULE
    // =========================

    Module createModule(
            Module module,
            String requesterEmail
    );

    // =========================
    // GET ALL MODULES
    // =========================

    List<Module> getAllModules();

    // =========================
    // GET MODULE BY ID
    // =========================

    Module getModuleById(Long id);

    // =========================
    // GET MODULES BY BATCH
    // =========================

    List<Module> getModulesByBatch(Long batchId);

    /** Modules for a batch as DTOs (mapping runs inside a read transaction for lazy loads). */
    List<ModuleResponse> getModuleResponsesByBatch(Long batchId);

    // =========================
    // GET MODULES BY TRAINER
    // =========================

    List<Module> getModulesByTrainer(Long trainerId);

    // =========================
    // GET MODULES BY EMAIL
    // =========================

    List<Module> getModulesForTrainerEmail(
            String trainerEmail
    );

    // =========================
    // ASSIGN / REASSIGN TRAINER
    // =========================

    Module assignTrainer(
            Long moduleId,
            Long trainerId,
            String requesterEmail
    );

    // =========================
    // SELF ASSIGN
    // =========================

    Module selfAssign(
            Long moduleId,
            String requesterEmail
    );

    // =========================
    // TRAINER SLOT VALIDATION
    // =========================

    void validateTrainerScheduleForBatch(
            Long trainerId,
            Long batchId,
            Long excludeModuleId
    );

    // =========================
    // UPDATE STATUS
    // =========================

    Module updateStatus(
            Long moduleId,
            String status,
            String requesterEmail
    );

    // =========================
    // UPDATE DETAILS
    // =========================

    Module updateDetails(
            Long moduleId,
            String name,
            String status,
            String requesterEmail
    );

    // =========================
    // UPDATE MODULE PROGRESS
    // =========================

    void updateModuleProgress(Long moduleId);

    // =========================
    // UPDATE BATCH PROGRESS
    // =========================

    void updateBatchProgress(Long batchId);

    // =========================
    // VALIDATE ACCESS
    // =========================

    void validateModuleManagementAccess(
            Long moduleId,
            String requesterEmail
    );

    // =========================
    // DELETE MODULE
    // =========================

    void deleteModule(
            Long moduleId,
            String requesterEmail
    );
}