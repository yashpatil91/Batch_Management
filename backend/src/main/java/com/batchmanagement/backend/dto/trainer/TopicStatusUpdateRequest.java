package com.batchmanagement.backend.dto.trainer;

import jakarta.validation.constraints.NotNull;

public class TopicStatusUpdateRequest {
    @NotNull
    private Boolean completed;

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
