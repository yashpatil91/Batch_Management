package com.batchmanagement.backend.repository;

import com.batchmanagement.backend.entity.Batch;
import com.batchmanagement.backend.entity.BatchTopic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchTopicRepository extends JpaRepository<BatchTopic, Long> {
    List<BatchTopic> findByBatch(Batch batch);
    long countByBatch(Batch batch);
    long countByBatchAndCompletedTrue(Batch batch);
}
