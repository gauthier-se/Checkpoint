package com.checkpoint.api.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.checkpoint.api.exceptions.CommentNotFoundException;
import com.checkpoint.api.repositories.CommentRepository;
import com.checkpoint.api.services.impl.AdminCommentServiceImpl;

@ExtendWith(MockitoExtension.class)
class AdminCommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    private AdminCommentServiceImpl adminCommentService;

    @BeforeEach
    void setUp() {
        adminCommentService = new AdminCommentServiceImpl(commentRepository);
    }

    @Test
    @DisplayName("deleteComment should delete when comment exists")
    void deleteComment_shouldDeleteWhenCommentExists() {
        // Given
        UUID commentId = UUID.randomUUID();
        when(commentRepository.existsById(commentId)).thenReturn(true);

        // When
        adminCommentService.deleteComment(commentId);

        // Then
        verify(commentRepository).existsById(commentId);
        verify(commentRepository).deleteById(commentId);
    }

    @Test
    @DisplayName("deleteComment should throw CommentNotFoundException when comment does not exist")
    void deleteComment_shouldThrowCommentNotFoundExceptionWhenNotFound() {
        // Given
        UUID commentId = UUID.randomUUID();
        when(commentRepository.existsById(commentId)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> adminCommentService.deleteComment(commentId))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessageContaining(commentId.toString());

        verify(commentRepository).existsById(commentId);
        verify(commentRepository, never()).deleteById(commentId);
    }
}
