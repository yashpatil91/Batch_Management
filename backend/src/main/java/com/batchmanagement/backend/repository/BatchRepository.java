package com.batchmanagement.backend.repository;

import com.batchmanagement.backend.entity.Batch;
import com.batchmanagement.backend.entity.User;
import com.batchmanagement.backend.entity.enums.BatchStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    List<Batch> findByTrainer(User trainer);

    long countByStatus(BatchStatus status);
    int countByTrainer(User trainer);
}
