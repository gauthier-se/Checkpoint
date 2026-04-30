package com.checkpoint.api.services.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.exceptions.CommentNotFoundException;
import com.checkpoint.api.repositories.CommentRepository;
import com.checkpoint.api.services.AdminCommentService;

/**
 * Implementation of {@link AdminCommentService} for admin comment management operations.
 */
@Service
@Transactional
public class AdminCommentServiceImpl implements AdminCommentService {

    private static final Logger log = LoggerFactory.getLogger(AdminCommentServiceImpl.class);

    private final CommentRepository commentRepository;

    public AdminCommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteComment(UUID commentId) {
        log.info("Admin deleting comment with id: {}", commentId);

        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException(commentId);
        }

        commentRepository.deleteById(commentId);
    }
}
