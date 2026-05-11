package com.batchmanagement.backend.controller;

import com.batchmanagement.backend.dto.common.ModuleResponse;
import com.batchmanagement.backend.entity.BatchTopic;
import com.batchmanagement.backend.entity.Module;
import com.batchmanagement.backend.entity.User;
import com.batchmanagement.backend.entity.enums.Role;
import com.batchmanagement.backend.mapper.ModuleMapper;
import com.batchmanagement.backend.repository.BatchTopicRepository;
import com.batchmanagement.backend.repository.UserRepository;
import com.batchmanagement.backend.service.ExcelSyllabusService;
import com.batchmanagement.backend.service.ModuleService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/modules")
@CrossOrigin(origins = "*")
public class ModuleController {

    private final ModuleService moduleService;
    private final ExcelSyllabusService excelSyllabusService;
    private final UserRepository userRepository;
    private final BatchTopicRepository batchTopicRepository;

    public ModuleController(ModuleService moduleService,
                            ExcelSyllabusService excelSyllabusService,
                            UserRepository userRepository,
                            BatchTopicRepository batchTopicRepository) {

        this.moduleService = moduleService;
        this.excelSyllabusService = excelSyllabusService;
        this.userRepository = userRepository;
        this.batchTopicRepository = batchTopicRepository;
    }

    // =========================
    // Create Module
    // =========================
    @PostMapping
    public ResponseEntity<ModuleResponse> createModule(@RequestBody Module module, Authentication authentication) {
        return ResponseEntity.ok(
                ModuleMapper.toResponse(moduleService.createModule(module, authentication.getName()))
        );
    }

    // =========================
    // Get All Modules
    // =========================
    @GetMapping
    public ResponseEntity<List<Module>> getAllModules() {
        return ResponseEntity.ok(moduleService.getAllModules());
    }

    // =========================
    // Get Module By ID
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<Module> getModuleById(@PathVariable Long id) {
        return ResponseEntity.ok(moduleService.getModuleById(id));
    }

    // =========================
    // Get Modules By Batch
    // =========================
    @GetMapping("/batch/{batchId}")
    public ResponseEntity<List<Module>> getModulesByBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(moduleService.getModulesByBatch(batchId));
    }

    // =========================
    // Get Modules By Trainer
    // =========================
    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<List<Module>> getModulesByTrainer(@PathVariable Long trainerId, Authentication authentication) {
        User requester = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (requester.getRole() == Role.TRAINER && !requester.getId().equals(trainerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Trainers can only view their own modules");
        }
        return ResponseEntity.ok(moduleService.getModulesByTrainer(trainerId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ModuleResponse>> getMyModules(Authentication authentication) {
        return ResponseEntity.ok(
                moduleService.getModulesForTrainerEmail(authentication.getName())
                        .stream()
                        .map(ModuleMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/trainers")
    public ResponseEntity<List<Map<String, Object>>> getTrainerDirectory() {
        List<Map<String, Object>> trainers = userRepository.findByRole(Role.TRAINER).stream()
                .map(trainer -> Map.<String, Object>of(
                        "id", trainer.getId(),
                        "name", trainer.getName(),
                        "email", trainer.getEmail(),
                        "expertise", trainer.getExpertise() == null ? "" : trainer.getExpertise()
                ))
                .toList();
        return ResponseEntity.ok(trainers);
    }

    // =========================
    // Assign/Reassign Trainer
    // =========================
    @PutMapping("/{moduleId}/assign-trainer/{trainerId}")
    public ResponseEntity<ModuleResponse> assignTrainer(
            @PathVariable Long moduleId,
            @PathVariable Long trainerId,
            Authentication authentication) {

        return ResponseEntity.ok(
                ModuleMapper.toResponse(moduleService.assignTrainer(moduleId, trainerId, authentication.getName()))
        );
    }

    @PutMapping("/{moduleId}/self-assign")
    public ResponseEntity<ModuleResponse> selfAssign(
            @PathVariable Long moduleId,
            Authentication authentication) {

        return ResponseEntity.ok(
                ModuleMapper.toResponse(moduleService.selfAssign(moduleId, authentication.getName()))
        );
    }

    // =========================
    // Update Module Status
    // =========================
    @PutMapping("/{moduleId}/status")
    public ResponseEntity<Module> updateStatus(
            @PathVariable Long moduleId,
            @RequestParam String status,
            Authentication authentication) {

        return ResponseEntity.ok(
                moduleService.updateStatus(moduleId, status, authentication.getName())
        );
    }

    @PutMapping("/{moduleId}")
    public ResponseEntity<ModuleResponse> updateModule(
            @PathVariable Long moduleId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        return ResponseEntity.ok(
                ModuleMapper.toResponse(
                        moduleService.updateDetails(
                                moduleId,
                                request.get("name"),
                                request.get("status"),
                                authentication.getName()
                        )
                )
        );
    }

 // =========================
 // Upload Excel Syllabus
 // =========================
 @PostMapping("/{moduleId}/upload-excel")
 public ResponseEntity<String> uploadExcel(
         @PathVariable Long moduleId,
         @RequestParam("file") MultipartFile file,
         Authentication authentication) {

     moduleService.validateModuleManagementAccess(moduleId, authentication.getName());

     Module module =
             moduleService.getModuleById(moduleId);

     excelSyllabusService.importTopicsFromExcel(
             file,
             module
     );

     return ResponseEntity.ok(
             "Syllabus uploaded successfully"
     );
 }

    @GetMapping("/{moduleId}/topics")
    public ResponseEntity<List<Map<String, Object>>> getTopicsByModule(
            @PathVariable Long moduleId,
            Authentication authentication) {
        moduleService.validateModuleManagementAccess(moduleId, authentication.getName());
        Module module = moduleService.getModuleById(moduleId);
        List<Map<String, Object>> topics = batchTopicRepository.findByModule(module).stream()
                .map(this::toTopicResponse)
                .toList();
        return ResponseEntity.ok(topics);
    }

    @PostMapping("/{moduleId}/topics")
    public ResponseEntity<Map<String, Object>> addTopicToModule(
            @PathVariable Long moduleId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        moduleService.validateModuleManagementAccess(moduleId, authentication.getName());
        Module module = moduleService.getModuleById(moduleId);

        String title = request.get("title");
        if (title == null || title.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic title is required");
        }

        BatchTopic topic = new BatchTopic();
        topic.setBatch(module.getBatch());
        topic.setModule(module);
        topic.setTitle(title.trim());
        topic.setCompleted(false);

        BatchTopic saved = batchTopicRepository.save(topic);
        moduleService.updateModuleProgress(moduleId);
        return ResponseEntity.ok(toTopicResponse(saved));
    }

    @PutMapping("/topics/{topicId}")
    public ResponseEntity<Map<String, Object>> updateTopicStatus(
            @PathVariable Long topicId,
            @RequestBody Map<String, Boolean> request,
            Authentication authentication) {
        BatchTopic topic = batchTopicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));

        if (topic.getModule() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic is not linked to a module");
        }

        moduleService.validateModuleManagementAccess(topic.getModule().getId(), authentication.getName());
        topic.setCompleted(Boolean.TRUE.equals(request.get("completed")));

        BatchTopic saved = batchTopicRepository.save(topic);
        moduleService.updateModuleProgress(topic.getModule().getId());
        return ResponseEntity.ok(toTopicResponse(saved));
    }

    @PutMapping("/topics/{topicId}/title")
    public ResponseEntity<Map<String, Object>> updateTopicTitle(
            @PathVariable Long topicId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        BatchTopic topic = batchTopicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));

        if (topic.getModule() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic is not linked to a module");
        }

        String title = request.get("title");
        if (title == null || title.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic title is required");
        }

        moduleService.validateModuleManagementAccess(topic.getModule().getId(), authentication.getName());
        topic.setTitle(title.trim());

        BatchTopic saved = batchTopicRepository.save(topic);
        return ResponseEntity.ok(toTopicResponse(saved));
    }

    @DeleteMapping("/topics/{topicId}")
    public ResponseEntity<Void> deleteTopic(
            @PathVariable Long topicId,
            Authentication authentication) {
        BatchTopic topic = batchTopicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));

        if (topic.getModule() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic is not linked to a module");
        }

        Long moduleId = topic.getModule().getId();
        moduleService.validateModuleManagementAccess(moduleId, authentication.getName());
        batchTopicRepository.delete(topic);
        moduleService.updateModuleProgress(moduleId);
        return ResponseEntity.ok().build();
    }

    private Map<String, Object> toTopicResponse(BatchTopic topic) {
        return Map.of(
                "id", topic.getId(),
                "title", topic.getTitle(),
                "completed", topic.isCompleted()
        );
    }
}
