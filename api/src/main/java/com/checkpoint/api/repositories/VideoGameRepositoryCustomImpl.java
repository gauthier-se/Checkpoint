package com.checkpoint.api.repositories;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.checkpoint.api.dto.catalog.GameCardDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

/**
 * Implementation of {@link VideoGameRepositoryCustom}.
 * Builds dynamic JPQL queries based on optional filter parameters while preserving
 * the constructor projection pattern used by the standard repository.
 */
@Repository
public class VideoGameRepositoryCustomImpl implements VideoGameRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<GameCardDto> findAllAsGameCardsWithFilters(Pageable pageable,
                                                            String genre,
                                                            String platform,
                                                            Integer yearMin,
                                                            Integer yearMax,
                                                            Double ratingMin,
                                                            Double ratingMax) {

        List<String> joins = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        Map<String, Object> parameters = new HashMap<>();

        joins.add("LEFT JOIN vg.rates r");

        if (genre != null && !genre.isBlank()) {
            joins.add("JOIN vg.genres g");
            conditions.add("LOWER(g.name) = LOWER(:genre)");
            parameters.put("genre", genre);
        }

        if (platform != null && !platform.isBlank()) {
            joins.add("JOIN vg.platforms p");
            conditions.add("LOWER(p.name) = LOWER(:platform)");
            parameters.put("platform", platform);
        }

        if (yearMin != null) {
            conditions.add("vg.releaseDate >= :yearMinDate");
            parameters.put("yearMinDate", LocalDate.of(yearMin, 1, 1));
        }

        if (yearMax != null) {
            conditions.add("vg.releaseDate <= :yearMaxDate");
            parameters.put("yearMaxDate", LocalDate.of(yearMax, 12, 31));
        }

        if (ratingMin != null) {
            conditions.add("vg.averageRating >= :ratingMin");
            parameters.put("ratingMin", ratingMin);
        }

        if (ratingMax != null) {
            conditions.add("vg.averageRating <= :ratingMax");
            parameters.put("ratingMax", ratingMax);
        }

        String joinClause = String.join(" ", joins);
        String whereClause = conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions);

        // Data query with constructor projection
        String dataJpql = "SELECT new com.checkpoint.api.dto.catalog.GameCardDto("
                + "vg.id, vg.title, vg.coverUrl, vg.releaseDate, vg.averageRating, COUNT(r.id)"
                + ") FROM VideoGame vg "
                + joinClause + " "
                + whereClause + " "
                + "GROUP BY vg.id, vg.title, vg.coverUrl, vg.releaseDate, vg.averageRating"
                + buildOrderByClause(pageable.getSort());

        TypedQuery<GameCardDto> dataQuery = entityManager.createQuery(dataJpql, GameCardDto.class);
        parameters.forEach(dataQuery::setParameter);
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        List<GameCardDto> content = dataQuery.getResultList();

        // Count query
        String countJpql = "SELECT COUNT(DISTINCT vg.id) FROM VideoGame vg "
                + joinClause + " "
                + whereClause;

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        parameters.forEach(countQuery::setParameter);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::getSingleResult);
    }

    /**
     * Builds the ORDER BY clause from a Spring Sort object.
     * Maps sort properties to JPQL-compatible expressions.
     *
     * @param sort the sort specification
     * @return the ORDER BY clause, or empty string if unsorted
     */
    private String buildOrderByClause(Sort sort) {
        if (sort.isUnsorted()) {
            return "";
        }

        List<String> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            String field = mapSortProperty(order.getProperty());
            String direction = order.isAscending() ? "ASC" : "DESC";
            orders.add(field + " " + direction);
        }

        return " ORDER BY " + String.join(", ", orders);
    }

    /**
     * Maps entity property names to JPQL expressions for ORDER BY.
     *
     * @param property the entity property name
     * @return the JPQL expression for ordering
     */
    private String mapSortProperty(String property) {
        return switch (property) {
            case "averageRating" -> "vg.averageRating";
            case "title" -> "vg.title";
            case "createdAt" -> "vg.createdAt";
            default -> "vg.releaseDate";
        };
    }
}
