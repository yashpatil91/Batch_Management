package com.batchmanagement.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "modules")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Parent Batch
    @JsonIgnoreProperties({
            "trainer",
            "topics"
    })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    // Module Name
    @Column(name = "module_name", nullable = false)
    private String name;

    // Assigned Trainer
    @JsonIgnoreProperties({
            "password"
    })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private User trainer;

    // Progress Percentage
    @Column(nullable = false)
    private Integer progress = 0;

    // NOT_STARTED / ONGOING / COMPLETED
    @Column(nullable = false)
    private String status = "NOT_STARTED";

    // Topics inside module
    @JsonIgnore
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BatchTopic> topics;

    // Trainer History
    @JsonIgnore
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModuleTrainerHistory> trainerHistories;

    // =========================
    // Constructors
    // =========================

    public Module() {
    }

    public Module(Long id,
                  Batch batch,
                  String name,
                  User trainer,
                  Integer progress,
                  String status,
                  List<BatchTopic> topics) {

        this.id = id;
        this.batch = batch;
        this.name = name;
        this.trainer = trainer;
        this.progress = progress;
        this.status = status;
        this.topics = topics;
    }

    // =========================
    // Getters and Setters
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getTrainer() {
        return trainer;
    }

    public void setTrainer(User trainer) {
        this.trainer = trainer;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<BatchTopic> getTopics() {
        return topics;
    }

    public void setTopics(List<BatchTopic> topics) {
        this.topics = topics;
    }

    public List<ModuleTrainerHistory> getTrainerHistories() {
        return trainerHistories;
    }

    public void setTrainerHistories(List<ModuleTrainerHistory> trainerHistories) {
        this.trainerHistories = trainerHistories;
    }
}