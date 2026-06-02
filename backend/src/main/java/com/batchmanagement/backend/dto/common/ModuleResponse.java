package com.batchmanagement.backend.dto.common;

import java.time.LocalDate;

public class ModuleResponse {

    private Long id;
    private String name;
    private Integer progress;
    private String status;
    private Long trainerId;
    private String trainerName;
    private Long batchId;
    private String batchName;
    private LocalDate batchStartDate;
    private LocalDate batchEndDate;
    private String batchTime;
    private String batchLabNo;
    private Integer batchNoOfStudents;
    private String batchMeetLink;
    private Integer batchProgress;
    private String batchStatus;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public LocalDate getBatchStartDate() { return batchStartDate; }
    public void setBatchStartDate(LocalDate batchStartDate) { this.batchStartDate = batchStartDate; }

    public LocalDate getBatchEndDate() { return batchEndDate; }
    public void setBatchEndDate(LocalDate batchEndDate) { this.batchEndDate = batchEndDate; }

    public String getBatchTime() { return batchTime; }
    public void setBatchTime(String batchTime) { this.batchTime = batchTime; }

    public String getBatchLabNo() { return batchLabNo; }
    public void setBatchLabNo(String batchLabNo) { this.batchLabNo = batchLabNo; }

    public Integer getBatchNoOfStudents() { return batchNoOfStudents; }
    public void setBatchNoOfStudents(Integer batchNoOfStudents) { this.batchNoOfStudents = batchNoOfStudents; }

    public String getBatchMeetLink() { return batchMeetLink; }
    public void setBatchMeetLink(String batchMeetLink) { this.batchMeetLink = batchMeetLink; }

    public Integer getBatchProgress() { return batchProgress; }
    public void setBatchProgress(Integer batchProgress) { this.batchProgress = batchProgress; }
    public String getBatchStatus() {
        return batchStatus;
    }

    public void setBatchStatus(String batchStatus) {
        this.batchStatus = batchStatus;
    }
}
 