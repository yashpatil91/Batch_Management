package com.batchmanagement.backend.dto.common;

import com.batchmanagement.backend.entity.enums.Role;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;

    // Constructor
    public UserResponse(Long id, String name, String email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
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
}