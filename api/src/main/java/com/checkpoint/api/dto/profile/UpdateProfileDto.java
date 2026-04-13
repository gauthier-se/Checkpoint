package com.checkpoint.api.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating a user's profile information.
 *
 * @param pseudo    the new display name (must be unique)
 * @param bio       the user's biography (max 500 characters)
 * @param isPrivate whether the profile should be private
 */
public record UpdateProfileDto(
        @NotBlank(message = "Pseudo is required")
        @Size(max = 30, message = "Pseudo must not exceed 30 characters")
        String pseudo,

        @Size(max = 500, message = "Bio must not exceed 500 characters")
        String bio,

        Boolean isPrivate
) {}
