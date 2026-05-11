package com.batchmanagement.backend.dto.common;

import com.batchmanagement.backend.entity.enums.BatchStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BatchWithModulesResponse {

    private Long id;
    private String domainName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer progress;
    private BatchStatus status;
    private String time;
    private String labNo;
    private Integer noOfStudents;
    private String meetLink;
    private List<ModuleResponse> modules = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDomainName() { return domainName; }
    public void setDomainName(String domainName) { this.domainName = domainName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

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

    public String getMeetLink() { return meetLink; }
    public void setMeetLink(String meetLink) { this.meetLink = meetLink; }

    public List<ModuleResponse> getModules() { return modules; }
    public void setModules(List<ModuleResponse> modules) { this.modules = modules; }
}
