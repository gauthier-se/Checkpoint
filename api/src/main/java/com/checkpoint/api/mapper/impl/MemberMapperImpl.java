package com.checkpoint.api.mapper.impl;

import org.springframework.stereotype.Component;

import com.checkpoint.api.dto.social.MemberCardDto;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.mapper.MemberMapper;

/**
 * Implementation of {@link MemberMapper}.
 */
@Component
public class MemberMapperImpl implements MemberMapper {

    /**
     * {@inheritDoc}
     */
    @Override
    public MemberCardDto toMemberCardDto(User user, Long followerCount, Long reviewCount, Boolean isFollowing) {
        if (user == null) {
            return null;
        }
        return new MemberCardDto(
                user.getId(),
                user.getPseudo(),
                user.getPicture(),
                user.getLevel(),
                followerCount != null ? followerCount : 0L,
                reviewCount != null ? reviewCount : 0L,
                isFollowing
        );
    }
}
