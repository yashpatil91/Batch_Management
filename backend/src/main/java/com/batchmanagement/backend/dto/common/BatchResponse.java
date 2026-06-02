package com.batchmanagement.backend.dto.common;

import com.batchmanagement.backend.entity.enums.BatchStatus;
import java.time.LocalDate;

public class BatchResponse {

    private Long id;
    private String domainName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long trainerId;
    private String trainerName;
    private Integer progress;
    private BatchStatus status;
    private String time;
    private String labNo;
    private Integer noOfStudents;
    private String meetLink;
    
    

    // GETTERS & SETTERS

    public String getMeetLink() {
		return meetLink;
	}
	public void setMeetLink(String meetLink) {
		this.meetLink = meetLink;
	}
	public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDomainName() { return domainName; }
    public void setDomainName(String domainName) { this.domainName = domainName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getLabNo() { return labNo; }
    public void setLabNo(String labNo) { this.labNo = labNo; }

    public Integer getNoOfStudents() { return noOfStudents; }
    public void setNoOfStudents(Integer noOfStudents) { this.noOfStudents = noOfStudents; }
}