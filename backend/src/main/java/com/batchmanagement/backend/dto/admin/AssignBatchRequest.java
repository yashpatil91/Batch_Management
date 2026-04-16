package com.batchmanagement.backend.dto.admin;

import jakarta.validation.constraints.NotNull;

public class AssignBatchRequest {
    @NotNull
    private Long batchId;

    @NotNull
    private Long trainerId;

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Long getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }
}
