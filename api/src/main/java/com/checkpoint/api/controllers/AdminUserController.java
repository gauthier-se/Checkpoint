package com.checkpoint.api.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.checkpoint.api.dto.admin.AdminUserDto;
import com.checkpoint.api.services.AdminUserService;

/**
 * REST controller for admin user management operations.
 * All endpoints require the {@code ROLE_ADMIN} authority.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * Returns all registered users.
     *
     * @return list of users with ID, username, and email
     */
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        log.info("Admin request: fetching all users");

        List<AdminUserDto> users = adminUserService.getAllUsers();

        log.info("Returning {} users", users.size());
        return ResponseEntity.ok(users);
    }
}
