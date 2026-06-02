package com.batchmanagement.backend.dto.common;

import com.batchmanagement.backend.entity.enums.Role;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String mobile;
    private Role role;
    private String expertise;
    private int totalBatches;

    // Constructor
    public UserResponse(Long id,
                        String name,
                        String email,
                        String mobile,
                        Role role,
                        String expertise,
                        int totalBatches) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.role = role;
        this.expertise = expertise;
        this.totalBatches = totalBatches;
    }

    // GETTERS

    public int getTotalBatches() {
        return totalBatches;
    }

    public void setTotalBatches(int totalBatches) {
        this.totalBatches = totalBatches;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public Role getRole() {
        return role;
    }

    public String getExpertise() {
        return expertise;
    }
}