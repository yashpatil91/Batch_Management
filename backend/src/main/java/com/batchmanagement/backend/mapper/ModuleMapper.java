package com.batchmanagement.backend.mapper;

import com.batchmanagement.backend.dto.common.ModuleResponse;
import com.batchmanagement.backend.entity.Batch;
import com.batchmanagement.backend.entity.Module;

public final class ModuleMapper {

    private ModuleMapper() {
    }

    public static ModuleResponse toResponse(Module module) {
        ModuleResponse response = new ModuleResponse();
        response.setId(module.getId());
        response.setName(module.getName());
        response.setProgress(module.getProgress());
        response.setStatus(module.getStatus());

        if (module.getTrainer() != null) {
            response.setTrainerId(module.getTrainer().getId());
            response.setTrainerName(module.getTrainer().getName());
        }

        Batch batch = module.getBatch();
        if (batch != null) {
            response.setBatchId(batch.getId());
            response.setBatchName(batch.getDomainName());
            response.setBatchStartDate(batch.getStartDate());
            response.setBatchEndDate(batch.getEndDate());
            response.setBatchTime(batch.getTime());
            response.setBatchLabNo(batch.getLabNo());
            response.setBatchNoOfStudents(batch.getNoOfStudents());
            response.setBatchMeetLink(batch.getMeetLink());
            response.setBatchProgress(batch.getProgress() != null ? batch.getProgress() : 0);
            response.setBatchStatus(
            	    batch.getStatus() != null
            	        ? batch.getStatus().name()
            	        : "ONGOING"
            	);
        }

        return response;
    }
}
