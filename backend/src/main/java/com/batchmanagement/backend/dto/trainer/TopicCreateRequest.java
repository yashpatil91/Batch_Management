package com.batchmanagement.backend.dto.trainer;

import jakarta.validation.constraints.NotBlank;

public class TopicCreateRequest {
    @NotBlank
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
