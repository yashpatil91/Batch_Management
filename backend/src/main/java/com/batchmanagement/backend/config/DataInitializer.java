package com.batchmanagement.backend.config;
import com.batchmanagement.backend.entity.enums.Role;
import com.batchmanagement.backend.entity.User;
import com.batchmanagement.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner initDefaultAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
	    return args -> {
	        String email = "admin@batch.com";

	        if (!userRepository.existsByEmail(email)) {

	            User admin = new User();

	            admin.setName("Default Admin");
	            admin.setEmail(email);
	            admin.setPassword(passwordEncoder.encode("admin123"));
	            admin.setRole(Role.ADMIN);

	            userRepository.save(admin);
	        }
	    };
	}}