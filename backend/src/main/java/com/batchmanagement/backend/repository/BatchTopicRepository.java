package com.batchmanagement.backend.repository;

import com.batchmanagement.backend.entity.Batch;
import com.batchmanagement.backend.entity.BatchTopic;
import com.batchmanagement.backend.entity.Module;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchTopicRepository extends JpaRepository<BatchTopic, Long> {

    // =========================
    // OLD BATCH METHODS
    // =========================

    List<BatchTopic> findByBatch(Batch batch);

    // =========================
    // NEW MODULE METHODS
    // =========================

    List<BatchTopic> findByModule(Module module);

    long countByModule(Module module);

    long countByModuleAndCompletedTrue(Module module);
}