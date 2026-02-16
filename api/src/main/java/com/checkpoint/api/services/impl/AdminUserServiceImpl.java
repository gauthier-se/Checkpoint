package com.checkpoint.api.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.checkpoint.api.dto.admin.AdminUserDto;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.services.AdminUserService;

/**
 * Implementation of {@link AdminUserService}.
 * Fetches all users from the database and maps them to admin DTOs.
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    public AdminUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<AdminUserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new AdminUserDto(
                        user.getId(),
                        user.getPseudo(),
                        user.getEmail()
                ))
                .toList();
    }
}
