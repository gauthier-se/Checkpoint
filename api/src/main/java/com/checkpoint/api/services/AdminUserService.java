package com.checkpoint.api.services;

import java.util.List;
import java.util.UUID;

import com.checkpoint.api.dto.admin.AdminUserDetailDto;
import com.checkpoint.api.dto.admin.AdminUserDto;
import com.checkpoint.api.dto.admin.AdminUserEditDto;

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

    /**
     * Returns detailed user profile information by ID.
     *
     * @param id the user's UUID
     * @return the detailed user profile
     */
    AdminUserDetailDto getUserById(UUID id);

    /**
     * Edits a user's profile fields (clear bio, clear picture, toggle private).
     *
     * @param id  the user's UUID
     * @param dto the edit fields
     * @return the updated user detail
     */
    AdminUserDetailDto editUser(UUID id, AdminUserEditDto dto);

    /**
     * Bans a user account, preventing them from logging in.
     *
     * @param id the user's UUID
     */
    void banUser(UUID id);

    /**
     * Unbans a user account, restoring their access.
     *
     * @param id the user's UUID
     */
    void unbanUser(UUID id);
}
