package com.batchmanagement.backend.entity;

import com.batchmanagement.backend.entity.enums.BatchStatus;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "batches")
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "domain_name", nullable = false)
    private String domainName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private User trainer;

    @Column(nullable = false)
    private Integer progress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status;

 // To this:
    @Column(name = "`time`", nullable = false)   // MySQL
    private String time;

    @Column(name = "lab_no", nullable = false)
    private String labNo;

    @Column(name = "no_of_students", nullable = false)
    private Integer noOfStudents;
    
    @Column(name = "meet_link")
    private String meetLink;

    // Constructor
    public Batch() {}

    public Batch(Long id, String domainName, LocalDate startDate, LocalDate endDate,
                 User trainer, Integer progress, BatchStatus status,
                 String time, String labNo, Integer noOfStudents, String meetLink) {
        this.id = id;
        this.domainName = domainName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.trainer = trainer;
        this.progress = progress;
        this.status = status;
        this.time = time;
        this.labNo = labNo;
        this.noOfStudents = noOfStudents;
        this.meetLink = meetLink;
        }

    public String getMeetLink() {
		return meetLink;
	}

	public void setMeetLink(String meetLink) {
		this.meetLink = meetLink;
	}

	// GETTERS
    public Long getId() { return id; }
    public String getDomainName() { return domainName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public User getTrainer() { return trainer; }
    public Integer getProgress() { return progress; }
    public BatchStatus getStatus() { return status; }
    public String getTime() { return time; }
    public String getLabNo() { return labNo; }
    public Integer getNoOfStudents() { return noOfStudents; }

    // SETTERS
    public void setId(Long id) { this.id = id; }
    public void setDomainName(String domainName) { this.domainName = domainName; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setTrainer(User trainer) { this.trainer = trainer; }
    public void setProgress(Integer progress) { this.progress = progress; }
    public void setStatus(BatchStatus status) { this.status = status; }
    public void setTime(String time) { this.time = time; }
    public void setLabNo(String labNo) { this.labNo = labNo; }
    public void setNoOfStudents(Integer noOfStudents) { this.noOfStudents = noOfStudents; }
}