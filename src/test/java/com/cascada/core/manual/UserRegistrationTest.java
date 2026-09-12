package com.cascada.core.manual;

import com.cascada.core.features.user.data.UserRepository;
import com.cascada.core.features.user.entity.UserEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class UserRegistrationTest {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("Cascada");
        EntityManager em = emf.createEntityManager();
        UserRepository userRepository = new UserRepository(em);

        System.out.println("=== Test 1: Valid registration ===");
        UserEntity user = new UserEntity("Asmaeel Ali", "asmaeel@example.com");
        userRepository.save(user);
        System.out.println("Saved: " + user.getName() + " | ID: " + user.getId());

        System.out.println("\n=== Test 2: Invalid email format ===");
        try {
            new UserEntity("Bad Email User", "not-an-email");
        } catch (IllegalArgumentException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }

        System.out.println("\n=== Test 3: Duplicate email check (manual) ===");
        boolean exists = userRepository.findByEmail("asmaeel@example.com").isPresent();
        if (exists) {
            System.out.println("Correctly detected duplicate email before insert.");
        } else {
            System.out.println("ERROR: duplicate not detected!");
        }

        em.close();
        emf.close();
    }
}