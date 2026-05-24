package com.checkpoint.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.checkpoint.api.services.SteamSignupTokenService.Claims;
import com.checkpoint.api.services.impl.SteamSignupTokenServiceImpl;

@DisplayName("SteamSignupTokenServiceImpl")
class SteamSignupTokenServiceImplTest {

    // 75-byte base64-encoded dev secret (mirrors application.properties default).
    private static final String SECRET = "dGhpcyBpcyBhIHZlcnkgbG9uZyBzZWNyZXQga2V5IGZvciBkZXZlbG9wbWVudCBvbmx5IGRvIG5vdCB1c2UgaW4gcHJvZHVjdGlvbg==";

    private SteamSignupTokenServiceImpl service(long ttlMs) {
        return new SteamSignupTokenServiceImpl(SECRET, ttlMs);
    }

    @Nested
    @DisplayName("issue + verify")
    class IssueVerifyTests {

        @Test
        @DisplayName("Should round-trip all Steam claims")
        void shouldRoundTripAllClaims() {
            SteamSignupTokenServiceImpl svc = service(600_000L);

            String token = svc.issue(
                    "76561198000000000",
                    "Persona",
                    "https://cdn/avatar.jpg",
                    "https://steamcommunity.com/id/persona");

            Optional<Claims> claims = svc.verify(token);

            assertThat(claims).isPresent();
            assertThat(claims.get().steamId()).isEqualTo("76561198000000000");
            assertThat(claims.get().steamDisplayName()).isEqualTo("Persona");
            assertThat(claims.get().steamAvatarUrl()).isEqualTo("https://cdn/avatar.jpg");
            assertThat(claims.get().steamProfileUrl()).isEqualTo("https://steamcommunity.com/id/persona");
        }

        @Test
        @DisplayName("Should round-trip when optional profile fields are null")
        void shouldRoundTripWithNullOptionalFields() {
            SteamSignupTokenServiceImpl svc = service(600_000L);

            String token = svc.issue("76561198000000000", null, null, null);

            Optional<Claims> claims = svc.verify(token);

            assertThat(claims).isPresent();
            assertThat(claims.get().steamId()).isEqualTo("76561198000000000");
            assertThat(claims.get().steamDisplayName()).isNull();
            assertThat(claims.get().steamAvatarUrl()).isNull();
            assertThat(claims.get().steamProfileUrl()).isNull();
        }

        @Test
        @DisplayName("Should reject null steamId at issuance")
        void shouldRejectNullSteamIdAtIssuance() {
            SteamSignupTokenServiceImpl svc = service(600_000L);

            assertThatThrownBy(() -> svc.issue(null, "p", null, null))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> svc.issue("  ", "p", null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("verify rejects bad tokens")
    class VerifyBadTokensTests {

        @Test
        @DisplayName("Should reject null / blank tokens")
        void shouldRejectNullOrBlank() {
            SteamSignupTokenServiceImpl svc = service(600_000L);

            assertThat(svc.verify(null)).isEmpty();
            assertThat(svc.verify("")).isEmpty();
            assertThat(svc.verify("   ")).isEmpty();
        }

        @Test
        @DisplayName("Should reject malformed tokens")
        void shouldRejectMalformedTokens() {
            SteamSignupTokenServiceImpl svc = service(600_000L);

            assertThat(svc.verify("not.a.jwt")).isEmpty();
            assertThat(svc.verify("garbage")).isEmpty();
        }

        @Test
        @DisplayName("Should reject tampered tokens")
        void shouldRejectTamperedTokens() {
            SteamSignupTokenServiceImpl svc = service(600_000L);

            String token = svc.issue("76561198000000000", "Persona", null, null);
            String tampered = token.substring(0, token.length() - 4) + "AAAA";

            assertThat(svc.verify(tampered)).isEmpty();
        }

        @Test
        @DisplayName("Should reject expired tokens")
        void shouldRejectExpiredTokens() throws InterruptedException {
            SteamSignupTokenServiceImpl svc = service(1L);

            String token = svc.issue("76561198000000000", "Persona", null, null);
            Thread.sleep(10);

            assertThat(svc.verify(token)).isEmpty();
        }

        @Test
        @DisplayName("Should reject tokens signed with a different key")
        void shouldRejectTokensFromDifferentKey() {
            SteamSignupTokenServiceImpl svcA = service(600_000L);
            // Different (valid base64) secret long enough for HS256.
            SteamSignupTokenServiceImpl svcB = new SteamSignupTokenServiceImpl(
                    "YW5vdGhlci12ZXJ5LWxvbmctc2VjcmV0LWtleS1mb3ItdGVzdGluZy1vbmx5LW5vdC1mb3ItcHJvZHVjdGlvbg==",
                    600_000L);

            String token = svcA.issue("76561198000000000", "Persona", null, null);

            assertThat(svcB.verify(token)).isEmpty();
        }
    }
}
