package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.profile.ProfileUpdatedDto;
import com.checkpoint.api.dto.profile.UpdateProfileDto;
import com.checkpoint.api.exceptions.PseudoAlreadyExistsException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.ProfileService;

/**
 * Unit tests for {@link MyProfileController}.
 */
@WebMvcTest(MyProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class MyProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Nested
    @DisplayName("PUT /api/me/profile")
    class UpdateProfile {

        @Test
        @DisplayName("Should update profile successfully")
        @WithMockUser(username = "alice@test.com")
        void updateProfile_shouldReturnUpdatedProfile() throws Exception {
            // Given
            UpdateProfileDto request = new UpdateProfileDto("newpseudo", "My new bio", false);
            ProfileUpdatedDto response = new ProfileUpdatedDto("newpseudo", "My new bio", null, false);

            when(profileService.updateProfile(eq("alice@test.com"), any(UpdateProfileDto.class)))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(put("/api/me/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("newpseudo"))
                    .andExpect(jsonPath("$.bio").value("My new bio"))
                    .andExpect(jsonPath("$.isPrivate").value(false));

            verify(profileService).updateProfile(eq("alice@test.com"), any(UpdateProfileDto.class));
        }

        @Test
        @DisplayName("Should return 400 when pseudo is blank")
        @WithMockUser(username = "alice@test.com")
        void updateProfile_shouldReturn400WhenPseudoBlank() throws Exception {
            // Given
            UpdateProfileDto request = new UpdateProfileDto("", "bio", false);

            // When / Then
            mockMvc.perform(put("/api/me/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 409 when pseudo already exists")
        @WithMockUser(username = "alice@test.com")
        void updateProfile_shouldReturn409WhenPseudoTaken() throws Exception {
            // Given
            UpdateProfileDto request = new UpdateProfileDto("takenpseudo", "bio", false);

            when(profileService.updateProfile(eq("alice@test.com"), any(UpdateProfileDto.class)))
                    .thenThrow(new PseudoAlreadyExistsException("takenpseudo"));

            // When / Then
            mockMvc.perform(put("/api/me/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("POST /api/me/picture")
    class UpdatePicture {

        @Test
        @DisplayName("Should upload picture successfully")
        @WithMockUser(username = "alice@test.com")
        void updatePicture_shouldReturnPictureUrl() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "avatar.jpg", "image/jpeg", "fake-image-data".getBytes());

            when(profileService.updatePicture(eq("alice@test.com"), any()))
                    .thenReturn("/uploads/profiles/uuid.jpg");

            // When / Then
            mockMvc.perform(multipart("/api/me/picture").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.picture").value("/uploads/profiles/uuid.jpg"));

            verify(profileService).updatePicture(eq("alice@test.com"), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/me/picture")
    class DeletePicture {

        @Test
        @DisplayName("Should delete picture successfully")
        @WithMockUser(username = "alice@test.com")
        void deletePicture_shouldReturn204() throws Exception {
            // Given
            doNothing().when(profileService).deletePicture("alice@test.com");

            // When / Then
            mockMvc.perform(delete("/api/me/picture"))
                    .andExpect(status().isNoContent());

            verify(profileService).deletePicture("alice@test.com");
        }
    }
}
