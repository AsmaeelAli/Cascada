package com.cascada.core.common.jparepo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

public abstract class JpaRepository<T, ID> implements Repository<T, ID> {

    protected final EntityManager entityManager;
    private final Class<T> entityClass;

    protected JpaRepository(EntityManager entityManager, Class<T> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
    }

    @Override
    public T save(T entity) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        try {
            if (entityManager.contains(entity)) {
                entity = entityManager.merge(entity); // تحديث entity موجودة
            } else {
                entityManager.persist(entity); // إدخال entity جديدة
            }
            tx.commit();
            return entity;
        } catch (RuntimeException e) {
            tx.rollback();
            throw e;
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        T entity = entityManager.find(entityClass, id);
        return Optional.ofNullable(entity);
    }

    @Override
    public List<T> findAll() {
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        return entityManager.createQuery(jpql, entityClass).getResultList();
    }

    @Override
    public void deleteById(ID id) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        try {
            T entity = entityManager.find(entityClass, id);
            if (entity != null) {
                entityManager.remove(entity);
            }
            tx.commit();
        } catch (RuntimeException e) {
            tx.rollback();
            throw e;
        }
    }
}