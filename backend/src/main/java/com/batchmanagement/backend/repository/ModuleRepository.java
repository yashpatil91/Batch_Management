package com.batchmanagement.backend.repository;

import com.batchmanagement.backend.entity.Module;
import com.batchmanagement.backend.entity.Batch;
import com.batchmanagement.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    // Get all modules of a batch
    List<Module> findByBatch(Batch batch);

    // Get all modules assigned to trainer
    List<Module> findByTrainer(User trainer);

    @Query("SELECT m FROM Module m INNER JOIN FETCH m.batch WHERE m.trainer = :trainer")
    List<Module> findByTrainerFetchingBatch(@Param("trainer") User trainer);

    // Get modules by status
    List<Module> findByStatus(String status);

    // Get modules by batch id
    List<Module> findByBatchId(Long batchId);

    /** Batch + trainer associations loaded in one query (avoids lazy-init issues when mapping to DTOs). */
    @Query("SELECT DISTINCT m FROM Module m INNER JOIN FETCH m.batch b LEFT JOIN FETCH m.trainer WHERE b.id = :batchId")
    List<Module> findByBatchIdFetchingAssociations(@Param("batchId") Long batchId);
}