package com.batchmanagement.backend.entity;

import java.util.List;

import com.batchmanagement.backend.entity.enums.Role;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "mobile")
    private String mobile;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "expertise")
    private String expertise;

    // Default constructor
    public User() {}

    // Parameterized constructor
    public User(Long id, String name, String email, String mobile, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.password = password;
        this.role = role;
    }

    // GETTERS
    public Long getId() { return id; }

    public String getName() { return name; }

    public String getEmail() { return email; }

    public String getMobile() { return mobile; }

    public String getPassword() { return password; }

    public Role getRole() { return role; }

    public String getExpertise() { return expertise; }

    // SETTERS
    public void setId(Long id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setEmail(String email) { this.email = email; }

    public void setMobile(String mobile) { this.mobile = mobile; }

    public void setPassword(String password) { this.password = password; }

    public void setRole(Role role) { this.role = role; }

    public void setExpertise(String expertise) { this.expertise = expertise; }

    @OneToMany(mappedBy = "trainer", fetch = FetchType.EAGER)
    private List<Batch> batches;
}