package com.cascada.core.features.user.data;

import com.cascada.core.common.jparepo.JpaRepository;
import com.cascada.core.features.user.entity.UserEntity;
import jakarta.persistence.EntityManager;

import java.util.Optional;

public class UserRepository extends JpaRepository<UserEntity, Long> {

    public UserRepository(EntityManager entityManager) {
        super(entityManager, UserEntity.class);
    }

    public Optional<UserEntity> findByEmail(String email) {
        String jpql = "SELECT u FROM UserEntity u WHERE u.email = :email";
        return entityManager.createQuery(jpql, UserEntity.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }
}