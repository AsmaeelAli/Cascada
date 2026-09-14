package com.cascada.core.features.task.data;

import com.cascada.core.common.jparepo.JpaRepository;
import com.cascada.core.features.task.entity.TaskEntity;
import jakarta.persistence.EntityManager;
import java.util.List;

public class TaskRepository extends JpaRepository<TaskEntity, Long> {

    public TaskRepository(EntityManager entityManager) {
        super(entityManager, TaskEntity.class);
    }

    public List<TaskEntity> findByOwnerId(Long ownerId) {
        String jpql = "SELECT t FROM TaskEntity t WHERE t.owner.id = :ownerId";
        return entityManager.createQuery(jpql, TaskEntity.class)
                .setParameter("ownerId", ownerId)
                .getResultList();
    }

    public List<TaskEntity> findUnclaimed() {
        String jpql = "SELECT t FROM TaskEntity t WHERE t.owner IS NULL";
        return entityManager.createQuery(jpql, TaskEntity.class)
                .getResultList();
    }
}