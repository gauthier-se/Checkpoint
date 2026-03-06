package com.checkpoint.api.mapper.impl;

import org.springframework.stereotype.Component;

import com.checkpoint.api.dto.playlog.GamePlayLogRequestDto;
import com.checkpoint.api.dto.playlog.GamePlayLogResponseDto;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.UserGamePlay;
import com.checkpoint.api.mapper.GamePlayLogMapper;
import com.checkpoint.api.enums.PlayStatus;

@Component
public class GamePlayLogMapperImpl implements GamePlayLogMapper {

    @Override
    public GamePlayLogResponseDto toDto(UserGamePlay playLog) {
        if (playLog == null) {
            return null;
        }

        Boolean hasReview = null;
        String reviewPreview = null;

        Review review = playLog.getReview();
        if (review != null) {
            hasReview = true;
            String content = review.getContent();
            if (content != null && !content.isEmpty()) {
                reviewPreview = content.length() > 100 ? content.substring(0, 100) : content;
            }
        } else {
            hasReview = false;
        }

        return new GamePlayLogResponseDto(
                playLog.getId(),
                playLog.getVideoGame() != null ? playLog.getVideoGame().getId() : null,
                playLog.getVideoGame() != null ? playLog.getVideoGame().getTitle() : null,
                playLog.getVideoGame() != null ? playLog.getVideoGame().getCoverUrl() : null,
                playLog.getPlatform() != null ? playLog.getPlatform().getId() : null,
                playLog.getPlatform() != null ? playLog.getPlatform().getName() : null,
                playLog.getStatus(),
                playLog.getIsReplay(),
                playLog.getTimePlayed(),
                playLog.getStartDate(),
                playLog.getEndDate(),
                playLog.getOwnership(),
                playLog.getCreatedAt(),
                playLog.getUpdatedAt(),
                hasReview,
                reviewPreview
        );
    }

    @Override
    public UserGamePlay toEntity(GamePlayLogRequestDto request) {
        if (request == null) {
            return null;
        }

        UserGamePlay playLog = new UserGamePlay();

        if (request.status() != null) {
            playLog.setStatus(request.status());
        } else {
            playLog.setStatus(PlayStatus.ARE_PLAYING);
        }

        if (request.isReplay() != null) {
            playLog.setIsReplay(request.isReplay());
        } else {
            playLog.setIsReplay(false);
        }

        playLog.setStartDate(request.startDate());
        playLog.setEndDate(request.endDate());
        playLog.setTimePlayed(request.timePlayed());
        playLog.setOwnership(request.ownership());

        return playLog;
    }

    @Override
    public void updateEntityFromDto(GamePlayLogRequestDto request, UserGamePlay playLog) {
        if (request == null || playLog == null) {
            return;
        }

        // According to typical REST UPDATE semantics (PUT), we might want to update all fields.
        // But for partial updates (PATCH) we'd only update non-null fields. Let's do a full replacement/update
        // with defaults, or at least update what is provided. I'll update all explicitly provided fields.

        if (request.status() != null) {
            playLog.setStatus(request.status());
        } else {
             // In PUT, null might mean "reset to default", but let's be safe and keep it or set default.
             // We'll just overwrite.
             playLog.setStatus(PlayStatus.ARE_PLAYING);
        }

        if (request.isReplay() != null) {
            playLog.setIsReplay(request.isReplay());
        } else {
            playLog.setIsReplay(false);
        }

        playLog.setStartDate(request.startDate());
        playLog.setEndDate(request.endDate());
        playLog.setTimePlayed(request.timePlayed());
        playLog.setOwnership(request.ownership());
    }
}
