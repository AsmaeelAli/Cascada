package com.cascada.core.domain.exception;

public class UserNotFoundException extends GlobalException{

    public UserNotFoundException(String email) {
        super("Email: " + email + " was not found.");
    }
}
