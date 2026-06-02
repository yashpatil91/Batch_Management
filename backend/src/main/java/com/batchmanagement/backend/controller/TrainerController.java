package com.batchmanagement.backend.controller;

import com.batchmanagement.backend.dto.common.BatchRequest;
import com.batchmanagement.backend.dto.common.BatchResponse;
import com.batchmanagement.backend.dto.trainer.ProgressUpdateRequest;
import com.batchmanagement.backend.service.TrainerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainer")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @GetMapping("/batches")
    public ResponseEntity<List<BatchResponse>> getAssignedBatches(Authentication authentication) {
        return ResponseEntity.ok(trainerService.getAssignedBatches(authentication.getName()));
    }

    @PostMapping("/batches")
    public ResponseEntity<BatchResponse> createBatch(@Valid @RequestBody BatchRequest request, Authentication authentication) {
        return ResponseEntity.ok(trainerService.createBatch(authentication.getName(), request));
    }

    @PutMapping("/progress/{id}")
    public ResponseEntity<BatchResponse> updateProgress(
            @PathVariable Long id,
            @Valid @RequestBody ProgressUpdateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(trainerService.updateProgress(authentication.getName(), id, request));
    }

    @PutMapping("/complete/{id}")
    public ResponseEntity<BatchResponse> markCompleted(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(trainerService.markComplete(authentication.getName(), id));
    }

    @PutMapping("/batches/{id}")
    public ResponseEntity<BatchResponse> updateBatch(
            @PathVariable Long id,
            @Valid @RequestBody BatchRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
            trainerService.updateBatch(authentication.getName(), id, request)
        );
    }
    // to delete a batch from trainer dashboard
    @DeleteMapping("/batches/{id}")
    public ResponseEntity<?> deleteBatch(
            @PathVariable Long id,
            Authentication authentication
    ) {
        trainerService.deleteBatch(authentication.getName(), id);
        return ResponseEntity.ok("Batch deleted successfully");
    }

    
}
