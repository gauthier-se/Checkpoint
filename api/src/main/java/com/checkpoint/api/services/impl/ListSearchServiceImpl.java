package com.checkpoint.api.services.impl;

import java.util.List;
import java.util.UUID;

import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.sort.dsl.SearchSortFactory;
import org.hibernate.search.engine.search.sort.dsl.SortFinalStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkpoint.api.dto.list.GameListCardDto;
import com.checkpoint.api.dto.list.GameListSearchCriteria;
import com.checkpoint.api.entities.GameList;
import com.checkpoint.api.entities.GameListEntry;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.mapper.GameListMapper;
import com.checkpoint.api.repositories.CommentRepository;
import com.checkpoint.api.repositories.GameListEntryRepository;
import com.checkpoint.api.repositories.LikeRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.services.ListSearchService;

import jakarta.persistence.EntityManager;

/**
 * Implementation of {@link ListSearchService} using Hibernate Search with Lucene backend.
 * Mirrors {@link NewsSearchServiceImpl} for {@link GameList}, adding visibility / author /
 * minGames filters and recent / popular / most-games sorts.
 */
@Service
@Transactional(readOnly = true)
public class ListSearchServiceImpl implements ListSearchService {

    private static final Logger log = LoggerFactory.getLogger(ListSearchServiceImpl.class);

    private static final int FUZZY_MAX_EDIT_DISTANCE = 2;

    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final GameListEntryRepository gameListEntryRepository;
    private final GameListMapper gameListMapper;

    public ListSearchServiceImpl(EntityManager entityManager,
                                 UserRepository userRepository,
                                 LikeRepository likeRepository,
                                 CommentRepository commentRepository,
                                 GameListEntryRepository gameListEntryRepository,
                                 GameListMapper gameListMapper) {
        this.entityManager = entityManager;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.gameListEntryRepository = gameListEntryRepository;
        this.gameListMapper = gameListMapper;
    }

    @Override
    public Page<GameListCardDto> search(GameListSearchCriteria criteria, Pageable pageable, String viewerEmail) {
        log.debug("Searching lists - criteria: {}, pageable: {}, viewer: {}",
                criteria, pageable, viewerEmail != null ? viewerEmail : "anonymous");

        UUID viewerId = resolveViewerId(criteria, viewerEmail);

        SearchSession searchSession = Search.session(entityManager);

        SearchResult<GameList> result = searchSession.search(GameList.class)
                .where(f -> buildPredicate(f, criteria, viewerId))
                .sort(f -> buildSort(f, criteria))
                .fetch((int) pageable.getOffset(), pageable.getPageSize());

        List<GameListCardDto> content = result.hits().stream()
                .map(this::toCardDto)
                .toList();

        long total = result.total().hitCount();
        log.debug("List search returned {}/{} results", content.size(), total);

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Resolves the viewer's UUID when {@code visibility=mine} is requested.
     * The controller is responsible for rejecting anonymous {@code mine} requests with 401;
     * here, an unknown viewer simply yields no results.
     */
    private UUID resolveViewerId(GameListSearchCriteria criteria, String viewerEmail) {
        if (!criteria.isMineVisibility() || viewerEmail == null) {
            return null;
        }
        return userRepository.findByEmail(viewerEmail).map(User::getId).orElse(null);
    }

    /**
     * Builds the boolean predicate combining the optional fuzzy text query and filters.
     */
    private BooleanPredicateClausesStep<?> buildPredicate(
            SearchPredicateFactory f,
            GameListSearchCriteria criteria,
            UUID viewerId
    ) {
        BooleanPredicateClausesStep<?> bool = f.bool();

        if (criteria.hasQuery()) {
            bool.must(f.match()
                    .fields("title", "description")
                    .matching(criteria.q())
                    .fuzzy(FUZZY_MAX_EDIT_DISTANCE));
        } else {
            bool.must(f.matchAll());
        }

        // Visibility: "mine" returns the viewer's lists (public + private);
        // anything else restricts to public lists only. Private lists never leak.
        if (criteria.isMineVisibility() && viewerId != null) {
            bool.filter(f.match().field("user.id").matching(viewerId));
        } else {
            bool.filter(f.match().field("isPrivate").matching(false));
        }

        if (criteria.author() != null && !criteria.author().isBlank()) {
            bool.filter(f.match().field("user.pseudo").matching(criteria.author()));
        }

        if (criteria.minGames() != null && criteria.minGames() > 0) {
            bool.filter(f.range().field("videoGamesCount").atLeast(criteria.minGames()));
        }

        return bool;
    }

    /**
     * Builds the sort clause based on criteria.sort() and whether a text query is present.
     * Default: createdAt desc; "relevance" only meaningful with a text query (falls back to recent).
     */
    private SortFinalStep buildSort(SearchSortFactory f, GameListSearchCriteria criteria) {
        String sort = criteria.sort();

        if (sort == null || sort.isBlank() || "recent".equalsIgnoreCase(sort)) {
            return f.field("createdAt").desc();
        }

        return switch (sort.toLowerCase()) {
            case "popular" -> f.field("likesCount").desc();
            case "most-games" -> f.field("videoGamesCount").desc();
            case "relevance" -> criteria.hasQuery() ? f.score() : f.field("createdAt").desc();
            default -> f.field("createdAt").desc();
        };
    }

    /**
     * Builds a {@link GameListCardDto} from a {@link GameList} entity, fetching the like /
     * comment counts and first cover URLs needed for the card view.
     */
    private GameListCardDto toCardDto(GameList gameList) {
        long likesCount = likeRepository.countByGameListId(gameList.getId());
        long commentsCount = commentRepository.countByGameListId(gameList.getId());
        List<GameListEntry> entries = gameListEntryRepository.findByGameListIdOrderByPositionAsc(gameList.getId());
        List<String> coverUrls = entries.stream()
                .limit(4)
                .map(entry -> entry.getVideoGame().getCoverUrl())
                .toList();
        return gameListMapper.toCardDto(gameList, likesCount, commentsCount, coverUrls);
    }
}
