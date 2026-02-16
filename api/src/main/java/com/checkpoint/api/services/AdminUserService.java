package com.checkpoint.api.services;

import java.util.List;

import com.checkpoint.api.dto.admin.AdminUserDto;

/**
 * Service interface for admin user management operations.
 */
public interface AdminUserService {

    /**
     * Returns all registered users as lightweight DTOs.
     *
     * @return list of all users
     */
    List<AdminUserDto> getAllUsers();
}
