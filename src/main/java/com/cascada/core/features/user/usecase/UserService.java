package com.cascada.core.features.user.usecase;

import com.cascada.core.domain.exception.DuplicateUserException;
import com.cascada.core.domain.exception.EntityNotFoundException;
import com.cascada.core.domain.exception.GlobalException;
import com.cascada.core.domain.exception.UserNotFoundException;
import com.cascada.core.features.user.data.UserRepository;
import com.cascada.core.features.user.entity.UserEntity;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity registerUser(String name, String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateUserException(email);
        }
        UserEntity user = new UserEntity(name, email);
        return userRepository.save(user);
    }

    public UserEntity login(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}