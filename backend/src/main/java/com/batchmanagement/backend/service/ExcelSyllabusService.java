package com.batchmanagement.backend.service;

import com.batchmanagement.backend.entity.BatchTopic;
import com.batchmanagement.backend.entity.Module;

import com.batchmanagement.backend.repository.BatchTopicRepository;

import org.apache.poi.ss.usermodel.*;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class ExcelSyllabusService {

    private final BatchTopicRepository batchTopicRepository;
    private final ModuleService moduleService;

    public ExcelSyllabusService(
            BatchTopicRepository batchTopicRepository,
            ModuleService moduleService) {

        this.batchTopicRepository = batchTopicRepository;
        this.moduleService = moduleService;
    }

    public void importTopicsFromExcel(
            MultipartFile file,
            Module module) {

        try {

            InputStream inputStream =
                    file.getInputStream();

            Workbook workbook =
                    WorkbookFactory.create(inputStream);
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            Sheet sheet =
                    workbook.getSheetAt(0);

            for (Row row : sheet) {

                Cell cell = row.getCell(0);

                if (cell == null) continue;

                String topicTitle =
                        formatter.formatCellValue(cell, evaluator);

                if (topicTitle == null ||
                        topicTitle.trim().isEmpty()) {

                    continue;
                }

                BatchTopic topic = new BatchTopic();

                topic.setTitle(topicTitle);

                topic.setCompleted(false);

                topic.setBatch(module.getBatch());

                topic.setModule(module);

                batchTopicRepository.save(topic);
            }

            workbook.close();
            moduleService.updateModuleProgress(module.getId());

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to import Excel syllabus"
            );
        }
    }
}