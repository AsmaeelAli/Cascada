package com.cascada.core.features.user.entity;

import com.cascada.core.validation.EmailValidator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    protected UserEntity() {
        // JPA فقط
    }

    public UserEntity(String name, String email) {
        this.name = validateName(name);
        this.email = validateEmail(email);
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

    public void changeEmail(String newEmail) {
        this.email = validateEmail(newEmail);
    }

    public void changeName(String newName) {
        this.name = validateName(newName);
    }

    private String validateEmail(String email) {
        if (!EmailValidator.isValid(email)) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        return email;
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        return name;
    }
}