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

    public BatchResponse createBatch(String trainerEmail, BatchRequest request) {

        User trainer = findTrainerByEmail(trainerEmail);

        Batch batch = new Batch();

        batch.setDomainName(request.getDomainName());
        batch.setStartDate(request.getStartDate());
        batch.setEndDate(request.getEndDate());
        batch.setTrainer(trainer);
        batch.setProgress(request.getProgress() == null ? 0 : request.getProgress());
        batch.setStatus(BatchStatus.ONGOING);
        batch.setTime(request.getTime());
        batch.setLabNo(request.getLabNo());
        batch.setNoOfStudents(request.getNoOfStudents());

        return BatchMapper.toResponse(batchRepository.save(batch));
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
            batch.setStatus(BatchStatus.ONGOING);
            batchRepository.save(batch);
            return;
        }

        long completed = batchTopicRepository.countByBatchAndCompletedTrue(batch);
        int progress = (int) Math.round((completed * 100.0) / total);
        batch.setProgress(progress);
        batch.setStatus(progress == 100 ? BatchStatus.COMPLETED : BatchStatus.ONGOING);
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
}