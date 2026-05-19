package com.checkpoint.api.services;

import java.util.UUID;

import com.checkpoint.api.dto.playlog.PlayLogDetailDto;

/**
 * Service for the public read-only view of play logs.
 *
 * <p>Distinct from {@link GamePlayLogService} which owns the authenticated user's
 * own play logs (CRUD under {@code /api/me/plays}).</p>
 */
public interface PlayLogService {

    /**
     * Returns the public detail of a play log.
     *
     * @param playId      the play log ID
     * @param viewerEmail the authenticated viewer's email, or {@code null} if anonymous
     * @return the play log detail DTO
     * @throws com.checkpoint.api.exceptions.PlayLogNotFoundException if the play log does not exist
     * @throws com.checkpoint.api.exceptions.ProfilePrivateException  if the author's profile is private
     *                                                                and the viewer is not the author
     */
    PlayLogDetailDto getPlayLogDetail(UUID playId, String viewerEmail);
}
