package com.batchmanagement.backend.dto.admin;

public class DashboardResponse {

    private long totalTrainers;
    private long totalBatches;
    private long ongoingBatches;

    public DashboardResponse() {}

    // GETTERS
    public long getTotalTrainers() { return totalTrainers; }
    public long getTotalBatches() { return totalBatches; }
    public long getOngoingBatches() { return ongoingBatches; }

    // SETTERS
    public void setTotalTrainers(long totalTrainers) { this.totalTrainers = totalTrainers; }
    public void setTotalBatches(long totalBatches) { this.totalBatches = totalBatches; }
    public void setOngoingBatches(long ongoingBatches) { this.ongoingBatches = ongoingBatches; }
}