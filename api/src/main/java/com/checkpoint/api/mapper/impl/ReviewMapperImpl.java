package com.checkpoint.api.mapper.impl;

import org.springframework.stereotype.Component;

import com.checkpoint.api.dto.catalog.ReviewResponseDto;
import com.checkpoint.api.dto.catalog.ReviewUserDto;
import com.checkpoint.api.entities.Review;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.mapper.ReviewMapper;

/**
 * Implementation of {@link ReviewMapper}.
 */
@Component
public class ReviewMapperImpl implements ReviewMapper {

    @Override
    public ReviewResponseDto toDto(Review review) {
        if (review == null) {
            return null;
        }

        ReviewUserDto userDto = null;
        if (review.getUser() != null) {
            User user = review.getUser();
            userDto = new ReviewUserDto(
                    user.getId(),
                    user.getPseudo(),
                    user.getPicture()
            );
        }

        return new ReviewResponseDto(
                review.getId(),
                review.getContent(),
                review.getHaveSpoilers(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                userDto
        );
    }
}
