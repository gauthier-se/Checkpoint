package com.checkpoint.api.mapper;

import com.checkpoint.api.dto.playlog.PlayLogDetailDto;
import com.checkpoint.api.dto.playlog.ReviewSummaryDto;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.UserGamePlay;

/**
 * Mapper that builds the public detail projection of a play log.
 *
 * <p>Like-counts, comment-counts and viewer flags are computed by the service and
 * passed in as parameters: the mapper itself is pure (no repository access).</p>
 */
public interface PlayLogDetailMapper {

    /**
     * Builds the detail DTO for a play log.
     *
     * @param playLog              the play log entity (must have its relations loaded)
     * @param reviewLikeCount      number of likes on the play's review (ignored if review null)
     * @param reviewCommentCount   number of comments on the play's review (ignored if review null)
     * @param isReviewLikedByViewer whether the viewer liked the review, or null if anonymous
     * @param isOwner              whether the viewer authored this play
     * @param isGameLikedByViewer  whether the viewer liked the game, or null if anonymous
     * @return the full detail DTO
     */
    PlayLogDetailDto toDto(
            UserGamePlay playLog,
            long reviewLikeCount,
            long reviewCommentCount,
            Boolean isReviewLikedByViewer,
            Boolean isOwner,
            Boolean isGameLikedByViewer);

    /**
     * Builds the nested review summary for a play log review.
     */
    ReviewSummaryDto toReviewSummary(
            Review review,
            long likeCount,
            long commentCount,
            Boolean isLikedByViewer);
}
