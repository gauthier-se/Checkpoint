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

import com.checkpoint.api.dto.catalog.GenreCatalogDto;
import com.checkpoint.api.entities.Genre;
import com.checkpoint.api.mapper.GenreCatalogMapper;
import com.checkpoint.api.repositories.GenreRepository;

/**
 * Unit tests for {@link GenreCatalogServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class GenreCatalogServiceImplTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private GenreCatalogMapper genreCatalogMapper;

    private GenreCatalogServiceImpl genreCatalogService;

    @BeforeEach
    void setUp() {
        genreCatalogService = new GenreCatalogServiceImpl(genreRepository, genreCatalogMapper);
    }

    @Test
    @DisplayName("getAllGenres should return mapped DTOs sorted by name")
    void getAllGenres_shouldReturnMappedDtos() {
        // Given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Genre genre1 = new Genre("Action");
        genre1.setId(id1);
        Genre genre2 = new Genre("RPG");
        genre2.setId(id2);

        List<Genre> genres = List.of(genre1, genre2);
        List<GenreCatalogDto> expectedDtos = List.of(
                new GenreCatalogDto(id1, "Action", 10),
                new GenreCatalogDto(id2, "RPG", 5)
        );

        when(genreRepository.findAll(any(Sort.class))).thenReturn(genres);
        when(genreCatalogMapper.toDtoList(genres)).thenReturn(expectedDtos);

        // When
        List<GenreCatalogDto> result = genreCatalogService.getAllGenres();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Action");
        assertThat(result.get(1).name()).isEqualTo("RPG");

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(genreRepository).findAll(sortCaptor.capture());
        Sort capturedSort = sortCaptor.getValue();
        assertThat(capturedSort.getOrderFor("name")).isNotNull();
        assertThat(capturedSort.getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("getAllGenres should return empty list when no genres exist")
    void getAllGenres_shouldReturnEmptyList() {
        // Given
        when(genreRepository.findAll(any(Sort.class))).thenReturn(List.of());
        when(genreCatalogMapper.toDtoList(anyList())).thenReturn(List.of());

        // When
        List<GenreCatalogDto> result = genreCatalogService.getAllGenres();

        // Then
        assertThat(result).isEmpty();
        verify(genreRepository).findAll(any(Sort.class));
    }
}
