package com.checkpoint.api.mapper.impl;

import org.springframework.stereotype.Component;

import com.checkpoint.api.dto.catalog.RateResponseDto;
import com.checkpoint.api.entities.Rate;
import com.checkpoint.api.mapper.RateMapper;

/**
 * Implementation of {@link RateMapper}.
 */
@Component
public class RateMapperImpl implements RateMapper {

    @Override
    public RateResponseDto toDto(Rate rate) {
        if (rate == null) {
            return null;
        }

        return new RateResponseDto(
                rate.getId(),
                rate.getScore(),
                rate.getVideoGame().getId(),
                rate.getCreatedAt(),
                rate.getUpdatedAt()
        );
    }
}
