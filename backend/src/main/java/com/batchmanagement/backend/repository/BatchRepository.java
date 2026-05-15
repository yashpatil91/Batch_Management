package com.batchmanagement.backend.repository;

import com.batchmanagement.backend.entity.Batch;
import com.batchmanagement.backend.entity.User;
import com.batchmanagement.backend.entity.enums.BatchStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    // All batches for history
    List<Batch> findByTrainer(User trainer);

    // Only active batches (ignores completed)
    List<Batch> findByTrainerAndStatusNot(User trainer, BatchStatus status);

    // Lab conflict check excluding completed
    List<Batch> findByLabNoAndStatusNot(String labNo, BatchStatus status);

    long countByStatus(BatchStatus status);

    // Active batch count only
    int countByTrainerAndStatusNot(User trainer, BatchStatus status);

    // Full count including history
    int countByTrainer(User trainer);

    @Modifying
    @Query(value = "DELETE FROM module_trainer_history WHERE module_id IN (SELECT id FROM modules WHERE batch_id = ?1)", nativeQuery = true)
    void deleteTrainerHistoryByBatchId(Long batchId);

    @Modifying
    @Query(value = "DELETE FROM batch_topics WHERE batch_id = ?1", nativeQuery = true)
    void deleteTopicsByBatchId(Long batchId);

    @Modifying
    @Query(value = "DELETE FROM modules WHERE batch_id = ?1", nativeQuery = true)
    void deleteModulesByBatchId(Long batchId);
}