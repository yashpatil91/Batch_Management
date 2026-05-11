package com.batchmanagement.backend.repository;

import com.batchmanagement.backend.entity.Module;
import com.batchmanagement.backend.entity.ModuleTrainerHistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleTrainerHistoryRepository
        extends JpaRepository<ModuleTrainerHistory, Long> {

    List<ModuleTrainerHistory> findByModule(Module module);
}