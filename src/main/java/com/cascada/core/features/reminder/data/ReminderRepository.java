package com.cascada.core.features.reminder.data;

import com.cascada.core.common.jparepo.JpaRepository;
import com.cascada.core.features.reminder.entity.ReminderEntity;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ReminderRepository extends JpaRepository<ReminderEntity, Long> {

    public ReminderRepository(EntityManager entityManager) {
        super(entityManager, ReminderEntity.class);
    }

    // مفيدة لاحقًا بمرحلة Multi-threading — نجيب بس التذكيرات يلي لسا ما انبعتت
    public List<ReminderEntity> findUnsent() {
        String jpql = "SELECT r FROM ReminderEntity r WHERE r.sent = false";
        return entityManager.createQuery(jpql, ReminderEntity.class)
                .getResultList();
    }
}