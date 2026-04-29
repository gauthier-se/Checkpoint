package com.checkpoint.api.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

/**
 * Builds the {@link ClientRegistrationRepository} programmatically so that
 * OAuth2 login is wired only when at least one provider has been configured
 * with both a non-empty client id and secret.
 *
 * <p>This avoids Spring Boot's standard property-based OAuth2 auto-configuration,
 * which would fail to start the application when the env-driven defaults are
 * empty (typical for tests or local dev without Doppler).</p>
 */
@Configuration
@Conditional(OAuth2ClientRegistrationConfig.OAuth2CredentialsPresent.class)
public class OAuth2ClientRegistrationConfig {

    private static final Logger log = LoggerFactory.getLogger(OAuth2ClientRegistrationConfig.class);

    private static final String DEFAULT_REDIRECT = "{baseUrl}/api/login/oauth2/code/{registrationId}";

    private final String googleClientId;
    private final String googleClientSecret;
    private final String twitchClientId;
    private final String twitchClientSecret;

    public OAuth2ClientRegistrationConfig(
            @Value("${oauth2.google.client-id:}") String googleClientId,
            @Value("${oauth2.google.client-secret:}") String googleClientSecret,
            @Value("${oauth2.twitch.client-id:}") String twitchClientId,
            @Value("${oauth2.twitch.client-secret:}") String twitchClientSecret) {
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.twitchClientId = twitchClientId;
        this.twitchClientSecret = twitchClientSecret;
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = new ArrayList<>();

        if (hasText(googleClientId) && hasText(googleClientSecret)) {
            registrations.add(CommonOAuth2Provider.GOOGLE.getBuilder("google")
                    .clientId(googleClientId)
                    .clientSecret(googleClientSecret)
                    .redirectUri(DEFAULT_REDIRECT)
                    .build());
            log.info("OAuth2 provider 'google' registered");
        }

        if (hasText(twitchClientId) && hasText(twitchClientSecret)) {
            registrations.add(ClientRegistration.withRegistrationId("twitch")
                    .clientId(twitchClientId)
                    .clientSecret(twitchClientSecret)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri(DEFAULT_REDIRECT)
                    .scope("user:read:email")
                    .authorizationUri("https://id.twitch.tv/oauth2/authorize")
                    .tokenUri("https://id.twitch.tv/oauth2/token")
                    .userInfoUri("https://api.twitch.tv/helix/users")
                    .userNameAttributeName("id")
                    .clientName("Twitch")
                    .build());
            log.info("OAuth2 provider 'twitch' registered");
        }

        return new InMemoryClientRegistrationRepository(registrations);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Activates this configuration only when at least one provider has both
     * a client id and a client secret configured. Evaluated in the
     * {@link ConfigurationPhase#REGISTER_BEAN REGISTER_BEAN} phase so it sees
     * fully-resolved property placeholders.
     */
    static class OAuth2CredentialsPresent implements ConfigurationCondition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String googleId = context.getEnvironment().getProperty("oauth2.google.client-id", "");
            String googleSecret = context.getEnvironment().getProperty("oauth2.google.client-secret", "");
            String twitchId = context.getEnvironment().getProperty("oauth2.twitch.client-id", "");
            String twitchSecret = context.getEnvironment().getProperty("oauth2.twitch.client-secret", "");
            boolean googleReady = !googleId.isBlank() && !googleSecret.isBlank();
            boolean twitchReady = !twitchId.isBlank() && !twitchSecret.isBlank();
            return googleReady || twitchReady;
        }

        @Override
        public ConfigurationPhase getConfigurationPhase() {
            return ConfigurationPhase.REGISTER_BEAN;
        }
    }
}
