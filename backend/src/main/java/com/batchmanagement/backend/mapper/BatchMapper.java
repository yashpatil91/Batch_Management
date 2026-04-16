package com.batchmanagement.backend.mapper;

import com.batchmanagement.backend.dto.common.BatchResponse;
import com.batchmanagement.backend.entity.Batch;

public final class BatchMapper {

    private BatchMapper() {
    }

    public static BatchResponse toResponse(Batch batch) {

        BatchResponse response = new BatchResponse();

        response.setId(batch.getId());
        response.setDomainName(batch.getDomainName());
        response.setStartDate(batch.getStartDate());
        response.setEndDate(batch.getEndDate());

        if (batch.getTrainer() != null) {
            response.setTrainerId(batch.getTrainer().getId());
            response.setTrainerName(batch.getTrainer().getName());
        }

        response.setProgress(batch.getProgress());
        response.setStatus(batch.getStatus());
        response.setTime(batch.getTime());
        response.setLabNo(batch.getLabNo());
        response.setNoOfStudents(batch.getNoOfStudents());

        return response;
    }
}