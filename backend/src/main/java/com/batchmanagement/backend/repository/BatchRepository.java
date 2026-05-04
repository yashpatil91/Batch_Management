package com.batchmanagement.backend.repository;

import com.batchmanagement.backend.entity.Batch;
import com.batchmanagement.backend.entity.User;
import com.batchmanagement.backend.entity.enums.BatchStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

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
}