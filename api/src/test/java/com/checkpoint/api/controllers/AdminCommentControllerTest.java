package com.checkpoint.api.controllers;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.exceptions.CommentNotFoundException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.AdminCommentService;

@WebMvcTest(AdminCommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminCommentService adminCommentService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Test
    @DisplayName("DELETE /api/admin/comments/{id} should return 204 No Content")
    void deleteComment_shouldReturn204() throws Exception {
        // Given
        UUID commentId = UUID.randomUUID();
        doNothing().when(adminCommentService).deleteComment(commentId);

        // When / Then
        mockMvc.perform(delete("/api/admin/comments/{id}", commentId))
                .andExpect(status().isNoContent());

        verify(adminCommentService).deleteComment(commentId);
    }

    @Test
    @DisplayName("DELETE /api/admin/comments/{id} should return 404 when comment does not exist")
    void deleteComment_shouldReturn404WhenCommentMissing() throws Exception {
        // Given
        UUID commentId = UUID.randomUUID();
        doThrow(new CommentNotFoundException(commentId))
                .when(adminCommentService).deleteComment(commentId);

        // When / Then
        mockMvc.perform(delete("/api/admin/comments/{id}", commentId))
                .andExpect(status().isNotFound());

        verify(adminCommentService).deleteComment(commentId);
    }
}
