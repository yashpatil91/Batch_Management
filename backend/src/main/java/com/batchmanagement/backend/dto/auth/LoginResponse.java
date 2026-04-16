package com.batchmanagement.backend.dto.auth;

import com.batchmanagement.backend.entity.enums.Role;

public class LoginResponse {

    private String token;
    private Long userId;
    private String name;
    private String email;
    private Role role;

    public LoginResponse() {}

    // GETTERS
    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }

    // SETTERS
    public void setToken(String token) { this.token = token; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(Role role) { this.role = role; }
}