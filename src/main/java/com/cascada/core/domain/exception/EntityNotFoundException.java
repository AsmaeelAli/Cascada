package com.cascada.core.domain.exception;

public class EntityNotFoundException extends GlobalException {

    public EntityNotFoundException(String entityName, Long id) {
        super(entityName + " with id " + id + " was not found.");
    }
}
