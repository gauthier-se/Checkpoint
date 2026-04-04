package com.checkpoint.api.mapper.impl;

import org.springframework.stereotype.Component;

import com.checkpoint.api.dto.tag.TagResponseDto;
import com.checkpoint.api.dto.tag.TagSummaryDto;
import com.checkpoint.api.entities.Tag;
import com.checkpoint.api.mapper.TagMapper;

/**
 * Implementation of {@link TagMapper}.
 */
@Component
public class TagMapperImpl implements TagMapper {

    /**
     * {@inheritDoc}
     */
    @Override
    public TagResponseDto toDto(Tag tag, long playLogsCount) {
        if (tag == null) {
            return null;
        }
        return new TagResponseDto(
                tag.getId(),
                tag.getName(),
                playLogsCount
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TagSummaryDto toSummaryDto(Tag tag) {
        if (tag == null) {
            return null;
        }
        return new TagSummaryDto(
                tag.getId(),
                tag.getName()
        );
    }
}
