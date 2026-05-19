package com.checkpoint.api.services.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.playlog.PlayLogDetailDto;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.entities.UserGamePlay;
import com.checkpoint.api.exceptions.PlayLogNotFoundException;
import com.checkpoint.api.exceptions.ProfilePrivateException;
import com.checkpoint.api.mapper.PlayLogDetailMapper;
import com.checkpoint.api.repositories.CommentRepository;
import com.checkpoint.api.repositories.LikeRepository;
import com.checkpoint.api.repositories.UserGamePlayRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.services.PlayLogService;

@Service
@Transactional(readOnly = true)
public class PlayLogServiceImpl implements PlayLogService {

    private static final Logger log = LoggerFactory.getLogger(PlayLogServiceImpl.class);

    private final UserGamePlayRepository userGamePlayRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PlayLogDetailMapper playLogDetailMapper;

    public PlayLogServiceImpl(
            UserGamePlayRepository userGamePlayRepository,
            UserRepository userRepository,
            LikeRepository likeRepository,
            CommentRepository commentRepository,
            PlayLogDetailMapper playLogDetailMapper) {
        this.userGamePlayRepository = userGamePlayRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.playLogDetailMapper = playLogDetailMapper;
    }

    @Override
    public PlayLogDetailDto getPlayLogDetail(UUID playId, String viewerEmail) {
        log.info("Fetching play log detail: {} (viewer={})", playId, viewerEmail);

        UserGamePlay playLog = userGamePlayRepository.findByIdWithRelations(playId)
                .orElseThrow(() -> new PlayLogNotFoundException("Play log not found with ID: " + playId));

        User author = playLog.getUser();
        UUID viewerId = null;
        boolean isOwner = false;

        if (viewerEmail != null) {
            User viewer = userRepository.findByEmail(viewerEmail).orElse(null);
            if (viewer != null) {
                viewerId = viewer.getId();
                isOwner = viewer.getId().equals(author.getId());
            }
        }

        if (Boolean.TRUE.equals(author.getIsPrivate()) && !isOwner) {
            throw new ProfilePrivateException(author.getPseudo());
        }

        Review review = playLog.getReview();
        long reviewLikeCount = 0L;
        long reviewCommentCount = 0L;
        Boolean isReviewLikedByViewer = null;

        if (review != null) {
            reviewLikeCount = likeRepository.countByReviewId(review.getId());
            reviewCommentCount = commentRepository.countByReviewId(review.getId());
            if (viewerId != null) {
                isReviewLikedByViewer = likeRepository.existsByUserIdAndReviewId(viewerId, review.getId());
            }
        }

        Boolean isGameLikedByViewer = null;
        if (viewerId != null && playLog.getVideoGame() != null) {
            isGameLikedByViewer = likeRepository.existsByUserIdAndVideoGameId(viewerId, playLog.getVideoGame().getId());
        }

        return playLogDetailMapper.toDto(
                playLog,
                reviewLikeCount,
                reviewCommentCount,
                isReviewLikedByViewer,
                isOwner,
                isGameLikedByViewer
        );
    }
}
