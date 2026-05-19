package com.checkpoint.api.mapper.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.checkpoint.api.dto.playlog.PlayLogDetailDto;
import com.checkpoint.api.dto.playlog.ReviewSummaryDto;
import com.checkpoint.api.dto.tag.TagSummaryDto;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.UserGamePlay;
import com.checkpoint.api.mapper.PlayLogDetailMapper;
import com.checkpoint.api.mapper.TagMapper;

@Component
public class PlayLogDetailMapperImpl implements PlayLogDetailMapper {

    private final TagMapper tagMapper;

    public PlayLogDetailMapperImpl(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    @Override
    public PlayLogDetailDto toDto(
            UserGamePlay playLog,
            long reviewLikeCount,
            long reviewCommentCount,
            Boolean isReviewLikedByViewer,
            Boolean isOwner,
            Boolean isGameLikedByViewer) {
        if (playLog == null) {
            return null;
        }

        List<TagSummaryDto> tags = playLog.getTags() != null
                ? playLog.getTags().stream().map(tagMapper::toSummaryDto).toList()
                : List.of();

        ReviewSummaryDto review = toReviewSummary(
                playLog.getReview(), reviewLikeCount, reviewCommentCount, isReviewLikedByViewer);

        return new PlayLogDetailDto(
                playLog.getId(),
                playLog.getCreatedAt(),
                playLog.getUpdatedAt(),
                playLog.getVideoGame() != null ? playLog.getVideoGame().getId() : null,
                playLog.getVideoGame() != null ? playLog.getVideoGame().getTitle() : null,
                playLog.getVideoGame() != null ? playLog.getVideoGame().getCoverUrl() : null,
                playLog.getVideoGame() != null ? playLog.getVideoGame().getReleaseDate() : null,
                playLog.getUser() != null ? playLog.getUser().getId() : null,
                playLog.getUser() != null ? playLog.getUser().getPseudo() : null,
                playLog.getUser() != null ? playLog.getUser().getPicture() : null,
                playLog.getStatus(),
                playLog.getIsReplay(),
                playLog.getTimePlayed(),
                playLog.getStartDate(),
                playLog.getEndDate(),
                playLog.getOwnership(),
                playLog.getPlatform() != null ? playLog.getPlatform().getId() : null,
                playLog.getPlatform() != null ? playLog.getPlatform().getName() : null,
                playLog.getScore(),
                tags,
                review,
                isOwner,
                isGameLikedByViewer
        );
    }

    @Override
    public ReviewSummaryDto toReviewSummary(
            Review review,
            long likeCount,
            long commentCount,
            Boolean isLikedByViewer) {
        if (review == null) {
            return null;
        }
        return new ReviewSummaryDto(
                review.getId(),
                review.getContent(),
                review.getHaveSpoilers(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                likeCount,
                commentCount,
                isLikedByViewer
        );
    }
}
