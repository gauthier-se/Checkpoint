package com.checkpoint.api.dto.admin;

import java.util.List;

/**
 * Summary of a bulk game import operation.
 *
 * @param totalFetched number of games returned by the external API
 * @param imported     number of new games persisted
 * @param skipped      number of games skipped because they already exist (deduplication by igdbId)
 * @param failed       number of games whose import threw an exception
 * @param errors       titles (or IGDB IDs when title is missing) of games that failed to import
 */
public record BulkImportResultDto(
        int totalFetched,
        int imported,
        int skipped,
        int failed,
        List<String> errors
) {}
