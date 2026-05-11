package com.batchmanagement.backend.repository;

import com.batchmanagement.backend.entity.Module;
import com.batchmanagement.backend.entity.Batch;
import com.batchmanagement.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    // Get all modules of a batch
    List<Module> findByBatch(Batch batch);

    // Get all modules assigned to trainer
    List<Module> findByTrainer(User trainer);

    // Get modules by status
    List<Module> findByStatus(String status);

    // Get modules by batch id
    List<Module> findByBatchId(Long batchId);
}