package com.batchmanagement.backend.dto.common;

import com.batchmanagement.backend.entity.enums.Role;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private String expertise;

    // Constructor
    public UserResponse(Long id, String name, String email, Role role, String expertise) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.expertise = expertise;
    }

    // GETTERS

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getExpertise() {
        return expertise;
    }
}