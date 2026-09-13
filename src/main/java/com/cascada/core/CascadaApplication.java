package com.cascada.core;

import com.cascada.core.features.user.data.UserRepository;
import com.cascada.core.features.user.entity.UserEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.Optional;

public class CascadaApplication {

    public static void main(String[] args) {

        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("Cascada");

        EntityManager em = emf.createEntityManager();

        UserRepository userRepository = new UserRepository(em);

        UserEntity testUser =
                new UserEntity("Asmaeel Ali", "asmaeel@example.com");

        userRepository.save(testUser);

        System.out.println("Saved user ID: " + testUser.getId());

        Optional<UserEntity> foundUser =
                userRepository.findByEmail("asmaeel@example.com");

        if (foundUser.isPresent()) {
            System.out.println("User found!");
            System.out.println("Name: " + foundUser.get().getName());
            System.out.println("Email: " + foundUser.get().getEmail());
        } else {
            System.out.println("User NOT found!");
        }

        em.close();
        emf.close();
    }
}
