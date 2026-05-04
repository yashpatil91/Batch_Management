package com.batchmanagement.backend.service;

import com.batchmanagement.backend.dto.common.BatchRequest;
import com.batchmanagement.backend.dto.common.BatchResponse;
import com.batchmanagement.backend.dto.trainer.ProgressUpdateRequest;
import com.batchmanagement.backend.dto.trainer.TopicCreateRequest;
import com.batchmanagement.backend.dto.trainer.TopicResponse;
import com.batchmanagement.backend.dto.trainer.TopicStatusUpdateRequest;
import com.batchmanagement.backend.entity.Batch;
import com.batchmanagement.backend.entity.BatchTopic;
import com.batchmanagement.backend.entity.User;
import com.batchmanagement.backend.entity.enums.BatchStatus;
import com.batchmanagement.backend.entity.enums.Role;
import com.batchmanagement.backend.exception.ResourceNotFoundException;
import com.batchmanagement.backend.mapper.BatchMapper;
import com.batchmanagement.backend.repository.BatchRepository;
import com.batchmanagement.backend.repository.BatchTopicRepository;
import com.batchmanagement.backend.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import com.batchmanagement.backend.exception.BadRequestException;
@Service
public class TrainerService {

    private final UserRepository userRepository;
    private final BatchRepository batchRepository;
    private final BatchTopicRepository batchTopicRepository;

    public TrainerService(UserRepository userRepository, BatchRepository batchRepository, BatchTopicRepository batchTopicRepository) {
        this.userRepository = userRepository;
        this.batchRepository = batchRepository;
        this.batchTopicRepository = batchTopicRepository;
    }

    public List<BatchResponse> getAssignedBatches(String trainerEmail) {
        User trainer = findTrainerByEmail(trainerEmail);

        return batchRepository.findByTrainer(trainer)
                .stream()
                .map(BatchMapper::toResponse)
                .toList();
    }

 // ================= CREATE BATCH =================
 // ================= CREATE BATCH =================
    public BatchResponse createBatch(String trainerEmail, BatchRequest request) {

        System.out.println("===== CREATE BATCH REQUEST =====");
        System.out.println("Trainer Email: " + trainerEmail);
        System.out.println("Domain Name: " + request.getDomainName());
        System.out.println("Start Date: " + request.getStartDate());
        System.out.println("End Date: " + request.getEndDate());
        System.out.println("Time: " + request.getTime());
        System.out.println("Lab No: " + request.getLabNo());
        System.out.println("Students: " + request.getNoOfStudents());

        User trainer = findTrainerByEmail(trainerEmail);

        // =========================
        // TRAINER CONFLICT CHECK
        // =========================
        List<Batch> activeBatches =
            batchRepository.findByTrainerAndStatusNot(
                trainer,
                BatchStatus.COMPLETED
            );

        for (Batch existing : activeBatches) {

            System.out.println("Checking trainer batch: " + existing.getDomainName());

            boolean dateOverlap =
                !request.getStartDate().isAfter(existing.getEndDate()) &&
                !request.getEndDate().isBefore(existing.getStartDate());

            if (!dateOverlap) continue;

            if (isTimeConflict(request.getTime(), existing.getTime())) {
                throw new BadRequestException(
                    "Trainer already has an active batch during this time."
                );
            }
        }

        // =========================
        // LAB CONFLICT CHECK
        // =========================
        List<Batch> labBatches =
            batchRepository.findByLabNoAndStatusNot(
                request.getLabNo(),
                BatchStatus.COMPLETED
            );

        for (Batch existing : labBatches) {

            System.out.println("Checking lab batch: " + existing.getDomainName());

            boolean dateOverlap =
                !request.getStartDate().isAfter(existing.getEndDate()) &&
                !request.getEndDate().isBefore(existing.getStartDate());

            if (!dateOverlap) continue;

            if (isTimeConflict(request.getTime(), existing.getTime())) {
                throw new BadRequestException(
                    "Lab already occupied during this time."
                );
            }
        }

        // =========================
        // CREATE BATCH
        // =========================
        Batch batch = new Batch();

        batch.setDomainName(request.getDomainName());
        batch.setStartDate(request.getStartDate());
        batch.setEndDate(request.getEndDate());
        batch.setTrainer(trainer);
        batch.setProgress(
            request.getProgress() == null ? 0 : request.getProgress()
        );
        batch.setStatus(BatchStatus.ONGOING);
        batch.setTime(request.getTime());
        batch.setLabNo(request.getLabNo());
        batch.setNoOfStudents(request.getNoOfStudents());

        System.out.println("Saving batch...");

        Batch savedBatch = batchRepository.save(batch);

        System.out.println("Batch saved successfully: " + savedBatch.getId());

        return BatchMapper.toResponse(savedBatch);
    }

    public BatchResponse updateProgress(String trainerEmail, Long batchId, ProgressUpdateRequest request) {

        Batch batch = findTrainerBatch(trainerEmail, batchId);

        batch.setProgress(request.getProgress());

        if (request.getProgress() == 100) {
            batch.setStatus(BatchStatus.COMPLETED);
        } else {
            batch.setStatus(BatchStatus.ONGOING);
        }

        return BatchMapper.toResponse(batchRepository.save(batch));
    }

    public BatchResponse markComplete(String trainerEmail, Long batchId) {

        Batch batch = findTrainerBatch(trainerEmail, batchId);

        batch.setProgress(100);
        batch.setStatus(BatchStatus.COMPLETED);

        return BatchMapper.toResponse(batchRepository.save(batch));
    }

    public List<TopicResponse> getTopics(String trainerEmail, Long batchId) {
        Batch batch = findTrainerBatch(trainerEmail, batchId);
        return batchTopicRepository.findByBatch(batch).stream().map(this::toTopicResponse).toList();
    }

    public TopicResponse addTopic(String trainerEmail, Long batchId, TopicCreateRequest request) {
        Batch batch = findTrainerBatch(trainerEmail, batchId);
        BatchTopic topic = new BatchTopic();
        topic.setBatch(batch);
        topic.setTitle(request.getTitle());
        topic.setCompleted(false);
        BatchTopic saved = batchTopicRepository.save(topic);
        recalculateProgress(batch);
        return toTopicResponse(saved);
    }

    public TopicResponse updateTopicStatus(String trainerEmail, Long topicId, TopicStatusUpdateRequest request) {
        BatchTopic topic = batchTopicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
        Batch batch = findTrainerBatch(trainerEmail, topic.getBatch().getId());
        topic.setCompleted(Boolean.TRUE.equals(request.getCompleted()));
        BatchTopic saved = batchTopicRepository.save(topic);
        recalculateProgress(batch);
        return toTopicResponse(saved);
    }

    private void recalculateProgress(Batch batch) {
        long total = batchTopicRepository.countByBatch(batch);

        if (total == 0) {
            batch.setProgress(0);
            batchRepository.save(batch);
            return;
        }

        long completed = batchTopicRepository.countByBatchAndCompletedTrue(batch);

        int progress = (int) Math.round((completed * 100.0) / total);

        batch.setProgress(progress);

        // ❌ DO NOT change status here

        batchRepository.save(batch);
    }

    private TopicResponse toTopicResponse(BatchTopic topic) {
        TopicResponse response = new TopicResponse();
        response.setId(topic.getId());
        response.setTitle(topic.getTitle());
        response.setCompleted(topic.isCompleted());
        return response;
    }

    private Batch findTrainerBatch(String trainerEmail, Long batchId) {

        User trainer = findTrainerByEmail(trainerEmail);

        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        if (batch.getTrainer() == null ||
                !batch.getTrainer().getId().equals(trainer.getId())) {
            throw new ResourceNotFoundException("Batch not assigned to this trainer");
        }

        return batch;
    }

    private User findTrainerByEmail(String email) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getRole() == Role.TRAINER)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));
    }
    ///adding things from here
    
    public void deleteTopic(String trainerEmail, Long topicId) {

        BatchTopic topic = batchTopicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        Batch batch = findTrainerBatch(trainerEmail, topic.getBatch().getId());

        batchTopicRepository.delete(topic);

        recalculateProgress(batch);
    }
    
    public TopicResponse updateTopicTitle(String trainerEmail, Long topicId, TopicCreateRequest request) {

        BatchTopic topic = batchTopicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        Batch batch = findTrainerBatch(trainerEmail, topic.getBatch().getId());

        topic.setTitle(request.getTitle());

        BatchTopic saved = batchTopicRepository.save(topic);

        return toTopicResponse(saved);
    }
    
    
    ///doing extra stuff, just in case for error handling
    
    public BatchResponse updateBatch(String trainerEmail, Long batchId, BatchRequest request) {

        Batch batch = findTrainerBatch(trainerEmail, batchId);

        User trainer = findTrainerByEmail(trainerEmail);

        // =========================
        // TRAINER CONFLICT CHECK
        // =========================
        List<Batch> activeBatches =
            batchRepository.findByTrainerAndStatusNot(
                trainer,
                BatchStatus.COMPLETED
            );

        for (Batch existing : activeBatches) {

            if (existing.getId().equals(batchId)) continue;

            boolean dateOverlap =
                !request.getStartDate().isAfter(existing.getEndDate()) &&
                !request.getEndDate().isBefore(existing.getStartDate());

            if (!dateOverlap) continue;

            if (isTimeConflict(request.getTime(), existing.getTime())) {
                throw new BadRequestException(
                    "Trainer already has an active batch during this time."
                );
            }
            
        }

        // =========================
        // LAB CONFLICT CHECK
        // =========================
        List<Batch> labBatches =
            batchRepository.findByLabNoAndStatusNot(
                request.getLabNo(),
                BatchStatus.COMPLETED
            );

        for (Batch existing : labBatches) {

            if (existing.getId().equals(batchId)) continue;

            boolean dateOverlap =
                !request.getStartDate().isAfter(existing.getEndDate()) &&
                !request.getEndDate().isBefore(existing.getStartDate());

            if (!dateOverlap) continue;

            if (isTimeConflict(request.getTime(), existing.getTime())) {
            	throw new BadRequestException(
                    "Lab already occupied during this time."
                );
            }
        }

        // =========================
        // UPDATE FIELDS
        // =========================
        batch.setDomainName(request.getDomainName());
        batch.setStartDate(request.getStartDate());
        batch.setEndDate(request.getEndDate());
        batch.setTime(request.getTime());
        batch.setLabNo(request.getLabNo());
        batch.setNoOfStudents(request.getNoOfStudents());

        Batch updated = batchRepository.save(batch);

        return BatchMapper.toResponse(updated);
    }
    public void deleteBatch(String trainerEmail, Long batchId) {

        // Validate trainer + batch ownership
        Batch batch = findTrainerBatch(trainerEmail, batchId);

        // 🔥 IMPORTANT: delete topics first (to avoid FK error)
        batchTopicRepository.deleteAll(
            batchTopicRepository.findByBatch(batch)
        );

        // Delete batch
        batchRepository.delete(batch);
    }
    
    
    private boolean isTimeConflict(String newTime, String existingTime) {

        // Safety check
        if (newTime == null || existingTime == null ||
            !newTime.contains("-") || !existingTime.contains("-")) {
            return false;
        }

        String[] newParts = newTime.split("-");
        String[] existingParts = existingTime.split("-");

        // Prevent invalid format crash
        if (newParts.length < 2 || existingParts.length < 2) {
            return false;
        }

        int newStart = convertToMinutes(newParts[0].trim());
        int newEnd   = convertToMinutes(newParts[1].trim());

        int oldStart = convertToMinutes(existingParts[0].trim());
        int oldEnd   = convertToMinutes(existingParts[1].trim());

        return newStart < oldEnd && newEnd > oldStart;
    }

    private int convertToMinutes(String time) {
        time = time.trim().toUpperCase();

        boolean isPM = time.contains("PM");
        boolean isAM = time.contains("AM");

        time = time.replace("AM", "").replace("PM", "").trim();

        String[] parts = time.split(":");

        int hour   = Integer.parseInt(parts[0].trim());
        int minute = Integer.parseInt(parts[1].trim()); // ✅ parts[1] is now clean "00", not "00 - 17"

        if (isPM && hour != 12) hour += 12;
        if (isAM && hour == 12) hour = 0;

        return hour * 60 + minute;
    }
}