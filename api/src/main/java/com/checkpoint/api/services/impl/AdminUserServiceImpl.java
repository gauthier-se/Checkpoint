package com.checkpoint.api.services.impl;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.admin.AdminUserDetailDto;
import com.checkpoint.api.dto.admin.AdminUserDto;
import com.checkpoint.api.dto.admin.AdminUserEditDto;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.exceptions.UserNotFoundException;
import com.checkpoint.api.repositories.ReportRepository;
import com.checkpoint.api.repositories.ReviewRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.services.AdminUserService;

/**
 * Implementation of {@link AdminUserService}.
 * Provides admin operations for user management including viewing, editing, and banning.
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserServiceImpl.class);

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;

    public AdminUserServiceImpl(UserRepository userRepository,
                                ReviewRepository reviewRepository,
                                ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.reportRepository = reportRepository;
    }

    @Override
    public List<AdminUserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new AdminUserDto(
                        user.getId(),
                        user.getPseudo(),
                        user.getEmail(),
                        user.getBanned()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        Long reviewCount = reviewRepository.countByUserId(id);
        Long reportCount = reportRepository.countReportsAgainstUser(id);

        return toDetailDto(user, reviewCount, reportCount);
    }

    @Override
    @Transactional
    public AdminUserDetailDto editUser(UUID id, AdminUserEditDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (Boolean.TRUE.equals(dto.clearBio())) {
            log.info("Admin clearing bio for user {}", id);
            user.setBio(null);
        }

        if (Boolean.TRUE.equals(dto.clearPicture())) {
            log.info("Admin clearing picture for user {}", id);
            user.setPicture(null);
        }

        if (dto.isPrivate() != null) {
            log.info("Admin setting isPrivate={} for user {}", dto.isPrivate(), id);
            user.setIsPrivate(dto.isPrivate());
        }

        User savedUser = userRepository.save(user);

        Long reviewCount = reviewRepository.countByUserId(id);
        Long reportCount = reportRepository.countReportsAgainstUser(id);

        return toDetailDto(savedUser, reviewCount, reportCount);
    }

    @Override
    @Transactional
    public void banUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        log.info("Admin banning user {} ({})", user.getPseudo(), id);
        user.setBanned(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unbanUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        log.info("Admin unbanning user {} ({})", user.getPseudo(), id);
        user.setBanned(false);
        userRepository.save(user);
    }

    private AdminUserDetailDto toDetailDto(User user, Long reviewCount, Long reportCount) {
        return new AdminUserDetailDto(
                user.getId(),
                user.getPseudo(),
                user.getEmail(),
                user.getBio(),
                user.getPicture(),
                user.getIsPrivate(),
                user.getBanned(),
                user.getXpPoint(),
                user.getLevel(),
                user.getCreatedAt(),
                reviewCount,
                reportCount
        );
    }
}
