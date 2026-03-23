package com.checkpoint.api.mapper.impl;

import org.springframework.stereotype.Component;

import com.checkpoint.api.dto.social.FollowUserDto;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.mapper.FollowMapper;

/**
 * Implementation of {@link FollowMapper}.
 */
@Component
public class FollowMapperImpl implements FollowMapper {

    /**
     * {@inheritDoc}
     */
    @Override
    public FollowUserDto toFollowUserDto(User user) {
        if (user == null) {
            return null;
        }
        return new FollowUserDto(
                user.getId(),
                user.getPseudo(),
                user.getPicture()
        );
    }
}
