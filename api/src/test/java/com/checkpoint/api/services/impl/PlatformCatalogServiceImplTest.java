package com.checkpoint.api.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.checkpoint.api.dto.catalog.PlatformCatalogDto;
import com.checkpoint.api.entities.Platform;
import com.checkpoint.api.mapper.PlatformCatalogMapper;
import com.checkpoint.api.repositories.PlatformRepository;

/**
 * Unit tests for {@link PlatformCatalogServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class PlatformCatalogServiceImplTest {

    @Mock
    private PlatformRepository platformRepository;

    @Mock
    private PlatformCatalogMapper platformCatalogMapper;

    private PlatformCatalogServiceImpl platformCatalogService;

    @BeforeEach
    void setUp() {
        platformCatalogService = new PlatformCatalogServiceImpl(platformRepository, platformCatalogMapper);
    }

    @Test
    @DisplayName("getAllPlatforms should return mapped DTOs sorted by name")
    void getAllPlatforms_shouldReturnMappedDtos() {
        // Given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Platform platform1 = new Platform("Nintendo Switch");
        platform1.setId(id1);
        Platform platform2 = new Platform("PlayStation 5");
        platform2.setId(id2);

        List<Platform> platforms = List.of(platform1, platform2);
        List<PlatformCatalogDto> expectedDtos = List.of(
                new PlatformCatalogDto(id1, "Nintendo Switch", 12),
                new PlatformCatalogDto(id2, "PlayStation 5", 30)
        );

        when(platformRepository.findAll(any(Sort.class))).thenReturn(platforms);
        when(platformCatalogMapper.toDtoList(platforms)).thenReturn(expectedDtos);

        // When
        List<PlatformCatalogDto> result = platformCatalogService.getAllPlatforms();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Nintendo Switch");
        assertThat(result.get(1).name()).isEqualTo("PlayStation 5");

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(platformRepository).findAll(sortCaptor.capture());
        Sort capturedSort = sortCaptor.getValue();
        assertThat(capturedSort.getOrderFor("name")).isNotNull();
        assertThat(capturedSort.getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("getAllPlatforms should return empty list when no platforms exist")
    void getAllPlatforms_shouldReturnEmptyList() {
        // Given
        when(platformRepository.findAll(any(Sort.class))).thenReturn(List.of());
        when(platformCatalogMapper.toDtoList(anyList())).thenReturn(List.of());

        // When
        List<PlatformCatalogDto> result = platformCatalogService.getAllPlatforms();

        // Then
        assertThat(result).isEmpty();
        verify(platformRepository).findAll(any(Sort.class));
    }
}
