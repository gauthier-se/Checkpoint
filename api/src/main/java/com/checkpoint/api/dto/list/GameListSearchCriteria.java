package com.checkpoint.api.dto.list;

/**
 * Criteria for searching and filtering game lists.
 * All fields are optional; null/blank values are ignored when building the query.
 *
 * @param q          free-text fuzzy query against title + description
 * @param sort       "recent" (default) | "popular" | "most-games" | "relevance"
 * @param visibility "public" (default) | "mine" (requires authenticated viewer)
 * @param author     filter by author pseudo (exact keyword match)
 * @param minGames   inclusive lower bound on the number of games in the list
 */
public record GameListSearchCriteria(
        String q,
        String sort,
        String visibility,
        String author,
        Integer minGames
) {
    public boolean hasQuery() {
        return q != null && !q.isBlank();
    }

    public boolean isMineVisibility() {
        return "mine".equalsIgnoreCase(visibility);
    }
}
