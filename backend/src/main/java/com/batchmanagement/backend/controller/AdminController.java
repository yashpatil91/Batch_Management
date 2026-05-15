	package com.batchmanagement.backend.controller;
	
	import com.batchmanagement.backend.dto.admin.AssignBatchRequest;
	import com.batchmanagement.backend.dto.admin.CreateBatchRequest;
	import com.batchmanagement.backend.dto.admin.DashboardResponse;
	import com.batchmanagement.backend.dto.common.BatchResponse;
	import com.batchmanagement.backend.dto.common.BatchWithModulesResponse;
	import com.batchmanagement.backend.dto.common.UserCreateRequest;
	import com.batchmanagement.backend.dto.common.UserResponse;
	import com.batchmanagement.backend.dto.common.UserUpdateRequest;
	
	import com.batchmanagement.backend.entity.User;
	import com.batchmanagement.backend.entity.enums.Role;
	
	import com.batchmanagement.backend.repository.UserRepository;
	
	import com.batchmanagement.backend.service.AdminService;
	import com.batchmanagement.backend.service.EmailService;
	
	import jakarta.validation.Valid;
	
	import java.util.ArrayList;
	import java.util.HashMap;
	import java.util.List;
	import java.util.Map;
	
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.http.ResponseEntity;
	
	import org.springframework.web.bind.annotation.CrossOrigin;
	import org.springframework.web.bind.annotation.DeleteMapping;
	import org.springframework.web.bind.annotation.GetMapping;
	import org.springframework.web.bind.annotation.PathVariable;
	import org.springframework.web.bind.annotation.PostMapping;
	import org.springframework.web.bind.annotation.PutMapping;
	import org.springframework.web.bind.annotation.RequestBody;
	import org.springframework.web.bind.annotation.RequestMapping;
	import org.springframework.web.bind.annotation.RestController;
	
	@RestController
	@RequestMapping("/api/admin")
	@CrossOrigin(origins = "*")
	public class AdminController {
	
	    private final AdminService adminService;
	    private final UserRepository userRepository;
	
	    @Autowired
	    private EmailService emailService;
	
	    public AdminController(AdminService adminService,
	                           UserRepository userRepository) {
	
	        this.adminService = adminService;
	        this.userRepository = userRepository;
	    }
	
	    // =========================
	    // TRAINERS
	    // =========================
	
	    @GetMapping("/trainers")
	    public ResponseEntity<List<UserResponse>> getTrainers() {
	
	        return ResponseEntity.ok(
	                adminService.getTrainers()
	        );
	    }
	
	    @PostMapping("/trainers")
	    public ResponseEntity<UserResponse> createTrainer(
	            @Valid @RequestBody UserCreateRequest request) {
	
	        return ResponseEntity.ok(
	                adminService.createTrainer(request)
	        );
	    }
	
	    @PutMapping("/trainers/{id}")
	    public ResponseEntity<UserResponse> updateTrainer(
	            @PathVariable Long id,
	            @Valid @RequestBody UserUpdateRequest request) {
	
	        return ResponseEntity.ok(
	                adminService.updateTrainer(id, request)
	        );
	    }
	
	    @DeleteMapping("/trainers/{id}")
	    public ResponseEntity<Void> deleteTrainer(
	            @PathVariable Long id) {
	
	        adminService.deleteTrainer(id);
	
	        return ResponseEntity.noContent().build();
	    }
	
	    // =========================
	    // GET ALL TRAINERS
	    // =========================
	
	    @GetMapping("/all-trainers")
	    public ResponseEntity<List<User>> getAllTrainers() {
	
	        List<User> trainers =
	                userRepository.findByRole(Role.TRAINER);
	
	        return ResponseEntity.ok(trainers);
	    }
	
	    // =========================
	    // PUBLIC TRAINERS
	    // =========================
	
	    @GetMapping("/public-trainers")
	    public ResponseEntity<List<User>> getPublicTrainers() {
	
	        List<User> trainers =
	                userRepository.findByRole(Role.TRAINER);
	
	        return ResponseEntity.ok(trainers);
	    }
	
	    // =========================
	    // ADMINS
	    // =========================
	
	    @GetMapping("/admins")
	    public ResponseEntity<List<UserResponse>> getAdmins() {
	
	        return ResponseEntity.ok(
	                adminService.getAdmins()
	        );
	    }
	
	    @PostMapping("/admins")
	    public ResponseEntity<UserResponse> createAdmin(
	            @Valid @RequestBody UserCreateRequest request) {
	
	        return ResponseEntity.ok(
	                adminService.createAdmin(request)
	        );
	    }
	
	    // =========================
	    // BATCHES
	    // =========================
	
	    @GetMapping("/batches")
	    public ResponseEntity<List<BatchResponse>> getBatches() {
	
	        return ResponseEntity.ok(
	                adminService.getAllBatches()
	        );
	    }
	
	    @GetMapping("/batches-with-modules")
	    public ResponseEntity<List<BatchWithModulesResponse>> getBatchesWithModules() {
	
	        return ResponseEntity.ok(
	                adminService.getAllBatchesWithModules()
	        );
	    }
	
	    @PostMapping("/assign-batch")
	    public ResponseEntity<BatchResponse> assignBatch(
	            @Valid @RequestBody AssignBatchRequest request) {
	
	        return ResponseEntity.ok(
	                adminService.assignBatch(request)
	        );
	    }
	
	    @PostMapping("/batches")
	    public ResponseEntity<BatchResponse> createBatch(
	            @Valid @RequestBody CreateBatchRequest request) {
	
	        return ResponseEntity.ok(
	                adminService.createBatch(request)
	        );
	    }
	
	    // =========================
	    // DASHBOARD
	    // =========================
	
	    @GetMapping("/dashboard")
	    public ResponseEntity<DashboardResponse> getDashboard() {
	
	        return ResponseEntity.ok(
	                adminService.getDashboard()
	        );
	    }
	
	    // =========================
	    // TEST EMAIL
	    // =========================
	
	    @GetMapping("/test-email")
	    public String testEmail() {
	
	        emailService.sendEmail(
	                "yourgmail@gmail.com",
	                "Test Email",
	                "Spring Boot Email Working"
	        );
	
	        return "Email Sent";
	    }
	
	    // =========================
	    // DELETE BATCH
	    // =========================
	
	    @DeleteMapping("/batches/{id}")
	    public ResponseEntity<Void> deleteBatch(
	            @PathVariable Long id) {
	
	        adminService.deleteBatch(id);
	
	        return ResponseEntity.noContent().build();
	    }
	
	    // =========================
	    // UPDATE BATCH
	    // =========================
	
	    @PutMapping("/batches/{id}")
	    public ResponseEntity<BatchResponse> updateBatch(
	            @PathVariable Long id,
	            @Valid @RequestBody CreateBatchRequest request) {
	
	        return ResponseEntity.ok(
	                adminService.updateBatch(id, request)
	        );
	    }
	
	    // =========================
	    // TRAINER PERFORMANCE
	    // =========================
	
	    @GetMapping("/trainers-performance")
	    public ResponseEntity<List<Map<String, Object>>> getTrainerPerformance() {
	
	        List<BatchWithModulesResponse> batches =
	                adminService.getAllBatchesWithModules();
	
	        Map<String, Integer> totalProgress = new HashMap<>();
	        Map<String, Integer> totalModules = new HashMap<>();	
	
	        for (BatchWithModulesResponse batch : batches) {
	
	            if (batch.getModules() == null) {
	                continue;
	            }
	
	            batch.getModules().forEach(module -> {
	
	            	String trainer =
	            	        module.getTrainerName() != null
	            	        ? module.getTrainerName()
	            	        : null;
	
	            	if (trainer == null) {
	            	    return;
	            	}
	
	            	int progress =
	            	        module.getProgress() != null
	            	        ? module.getProgress()
	            	        : 0;

	            	totalProgress.put(
	            	        trainer,
	            	        totalProgress.getOrDefault(trainer, 0) + progress
	            	);

	            	totalModules.put(
	            	        trainer,
	            	        totalModules.getOrDefault(trainer, 0) + 1
	            	);
	            });
	        }
	
	        List<Map<String, Object>> result = new ArrayList<>();
	
	        for (String trainer : totalModules.keySet()) {
	
	        	int progressSum =
	        	        totalProgress.getOrDefault(trainer, 0);

	        	int moduleCount =
	        	        totalModules.getOrDefault(trainer, 1);

	        	int performance =
	        	        (int) Math.round(
	        	        	    (double) progressSum / moduleCount
	        	        );
	
	            Map<String, Object> data = new HashMap<>();
	
	            data.put("trainerName", trainer);
	            data.put("performance", performance);

	            result.add(data);
	            }

	            result.sort((a, b) ->
	                    Integer.compare(
	                            (int) b.get("performance"),
	                            (int) a.get("performance")
	                    )
	            );

	            return ResponseEntity.ok(result);
	    }
	}