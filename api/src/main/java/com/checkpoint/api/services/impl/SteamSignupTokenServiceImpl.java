package com.checkpoint.api.services.impl;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.checkpoint.api.services.SteamSignupTokenService;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class SteamSignupTokenServiceImpl implements SteamSignupTokenService {

    private static final String TOKEN_TYPE = "steam_signup";
    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_STEAM_ID = "steamId";
    private static final String CLAIM_DISPLAY_NAME = "steamDisplayName";
    private static final String CLAIM_AVATAR_URL = "steamAvatarUrl";
    private static final String CLAIM_PROFILE_URL = "steamProfileUrl";
    private static final String CLAIM_NONCE = "nonce";

    private final SecretKey signingKey;
    private final long ttlMs;

    public SteamSignupTokenServiceImpl(
            @Value("${jwt.secret}") String secretKey,
            @Value("${steam.signup.token-ttl-ms:600000}") long ttlMs) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.ttlMs = ttlMs;
    }

    @Override
    public String issue(String steamId, String steamDisplayName, String steamAvatarUrl, String steamProfileUrl) {
        if (steamId == null || steamId.isBlank()) {
            throw new IllegalArgumentException("steamId is required");
        }

        var builder = Jwts.builder()
                .claim(CLAIM_TYPE, TOKEN_TYPE)
                .claim(CLAIM_STEAM_ID, steamId)
                .claim(CLAIM_NONCE, UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttlMs))
                .signWith(signingKey);

        if (steamDisplayName != null) {
            builder.claim(CLAIM_DISPLAY_NAME, steamDisplayName);
        }
        if (steamAvatarUrl != null) {
            builder.claim(CLAIM_AVATAR_URL, steamAvatarUrl);
        }
        if (steamProfileUrl != null) {
            builder.claim(CLAIM_PROFILE_URL, steamProfileUrl);
        }

        return builder.compact();
    }

    @Override
    public Optional<Claims> verify(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        try {
            io.jsonwebtoken.Claims raw = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!TOKEN_TYPE.equals(raw.get(CLAIM_TYPE, String.class))) {
                return Optional.empty();
            }

            String steamId = raw.get(CLAIM_STEAM_ID, String.class);
            if (steamId == null || steamId.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new Claims(
                    steamId,
                    raw.get(CLAIM_DISPLAY_NAME, String.class),
                    raw.get(CLAIM_AVATAR_URL, String.class),
                    raw.get(CLAIM_PROFILE_URL, String.class)
            ));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
