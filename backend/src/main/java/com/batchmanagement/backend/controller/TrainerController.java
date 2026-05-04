package com.batchmanagement.backend.controller;

import com.batchmanagement.backend.dto.common.BatchRequest;
import com.batchmanagement.backend.dto.common.BatchResponse;
import com.batchmanagement.backend.dto.trainer.ProgressUpdateRequest;
import com.batchmanagement.backend.dto.trainer.TopicCreateRequest;
import com.batchmanagement.backend.dto.trainer.TopicResponse;
import com.batchmanagement.backend.dto.trainer.TopicStatusUpdateRequest;
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

    @GetMapping("/batches/{id}/topics")
    public ResponseEntity<List<TopicResponse>> getTopics(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(trainerService.getTopics(authentication.getName(), id));
    }

    @PostMapping("/batches/{id}/topics")
    public ResponseEntity<TopicResponse> addTopic(
            @PathVariable Long id,
            @Valid @RequestBody TopicCreateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(trainerService.addTopic(authentication.getName(), id, request));
    }

    @PutMapping("/topics/{topicId}")
    public ResponseEntity<TopicResponse> updateTopicStatus(
            @PathVariable Long topicId,
            @Valid @RequestBody TopicStatusUpdateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(trainerService.updateTopicStatus(authentication.getName(), topicId, request));
    }
    
    
  
    @DeleteMapping("/topics/{topicId}")
    public ResponseEntity<?> deleteTopic(
            @PathVariable Long topicId,
            Authentication authentication
    ) {
        trainerService.deleteTopic(authentication.getName(), topicId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/topics/{topicId}/title")
    public ResponseEntity<?> updateTopicTitle(
            @PathVariable Long topicId,
            @RequestBody TopicCreateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
            trainerService.updateTopicTitle(authentication.getName(), topicId, request)
        );
    }
    
  ///adding things from here
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
