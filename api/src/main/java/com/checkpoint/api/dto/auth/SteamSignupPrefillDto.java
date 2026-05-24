package com.checkpoint.api.dto.auth;

/**
 * Prefill payload returned to the frontend on {@code GET /api/auth/steam/signup-prefill}.
 * Built from the claims of a verified Steam signup token. Display name, avatar URL and
 * profile URL may be {@code null} when the Steam API was unavailable at token-issuance time.
 *
 * @param steamId           the verified 64-bit SteamID as a string
 * @param steamDisplayName  the persona name fetched from Steam, or {@code null}
 * @param steamAvatarUrl    the medium avatar URL fetched from Steam, or {@code null}
 * @param steamProfileUrl   the public profile URL fetched from Steam, or {@code null}
 */
public record SteamSignupPrefillDto(
        String steamId,
        String steamDisplayName,
        String steamAvatarUrl,
        String steamProfileUrl
) {
}
