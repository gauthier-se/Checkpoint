package com.checkpoint.api.dto.steam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code POST /api/me/steam/link}.
 *
 * <p>Accepts any of the following input shapes; format detection and vanity
 * resolution happen in the service layer:
 * <ul>
 *   <li>a 17-digit SteamID64 (e.g. {@code 76561198000000000})</li>
 *   <li>a profile URL containing a SteamID64
 *       (e.g. {@code https://steamcommunity.com/profiles/76561198000000000/})</li>
 *   <li>a vanity name or vanity URL
 *       (e.g. {@code alice} or {@code https://steamcommunity.com/id/alice/})</li>
 * </ul>
 *
 * @param steamId the user-supplied Steam identifier
 */
public record LinkSteamRequestDto(
        @NotBlank
        @Size(max = 256, message = "steamId must be 256 characters or fewer")
        String steamId
) {}
