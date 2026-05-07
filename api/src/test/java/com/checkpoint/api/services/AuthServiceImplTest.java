package com.checkpoint.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.checkpoint.api.dto.auth.ForgotPasswordRequestDto;
import com.checkpoint.api.dto.auth.LoginRequestDto;
import com.checkpoint.api.dto.auth.RegisterRequestDto;
import com.checkpoint.api.dto.auth.ResetPasswordRequestDto;
import com.checkpoint.api.dto.auth.UserMeDto;
import com.checkpoint.api.entities.PasswordResetToken;
import com.checkpoint.api.entities.Role;
import com.checkpoint.api.exceptions.InvalidTokenException;
import com.checkpoint.api.exceptions.RegistrationConflictException;
import com.checkpoint.api.repositories.PasswordResetTokenRepository;
import com.checkpoint.api.repositories.RoleRepository;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.security.JwtService;
import com.checkpoint.api.services.impl.AuthServiceImpl;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl")
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailService emailService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                authenticationManager,
                jwtService,
                userDetailsService,
                userRepository,
                passwordEncoder,
                roleRepository,
                passwordResetTokenRepository,
                emailService,
                false,      // cookieSecure = false in tests
                86400000L   // jwtExpirationMs = 24h
        );
    }

    @Nested
    @DisplayName("authenticateAndGenerateToken")
    class AuthenticateAndGenerateTokenTests {

        @Test
        @DisplayName("Should authenticate and return JWT token")
        void shouldAuthenticateAndReturnToken() {
            // Given
            LoginRequestDto request = new LoginRequestDto("user@test.com", "password123");
            UserDetails userDetails = User.builder()
                    .username("user@test.com")
                    .password("encodedPassword")
                    .roles("USER")
                    .build();
            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userDetailsService.loadUserByUsername("user@test.com"))
                    .thenReturn(userDetails);
            when(jwtService.generateToken(userDetails))
                    .thenReturn("jwt.token.here");

            // When
            String token = authService.authenticateAndGenerateToken(request);

            // Then
            assertThat(token).isEqualTo("jwt.token.here");
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userDetailsService).loadUserByUsername("user@test.com");
            verify(jwtService).generateToken(userDetails);
        }

        @Test
        @DisplayName("Should throw BadCredentialsException for invalid credentials")
        void shouldThrowForInvalidCredentials() {
            // Given
            LoginRequestDto request = new LoginRequestDto("user@test.com", "wrongPassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // When / Then
            assertThatThrownBy(() -> authService.authenticateAndGenerateToken(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Bad credentials");

            verify(userDetailsService, never()).loadUserByUsername(any());
            verify(jwtService, never()).generateToken(any(UserDetails.class));
        }
    }

    @Nested
    @DisplayName("authenticateAndSetCookie")
    class AuthenticateAndSetCookieTests {

        @Test
        @DisplayName("Should authenticate and write checkpoint_token cookie on response")
        void shouldAuthenticateAndSetCookie() {
            // Given
            LoginRequestDto request = new LoginRequestDto("user@test.com", "password123");
            UserDetails userDetails = User.builder()
                    .username("user@test.com")
                    .password("encodedPassword")
                    .roles("USER")
                    .build();
            Authentication authentication = mock(Authentication.class);
            HttpServletResponse servletResponse = mock(HttpServletResponse.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userDetailsService.loadUserByUsername("user@test.com"))
                    .thenReturn(userDetails);
            when(jwtService.generateToken(userDetails))
                    .thenReturn("jwt.token.here");

            // When
            authService.authenticateAndSetCookie(request, servletResponse);

            // Then
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtService).generateToken(userDetails);

            ArgumentCaptor<String> headerNameCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);
            verify(servletResponse).addHeader(headerNameCaptor.capture(), headerValueCaptor.capture());

            assertThat(headerNameCaptor.getValue()).isEqualTo("Set-Cookie");
            String cookieHeader = headerValueCaptor.getValue();
            assertThat(cookieHeader).contains("checkpoint_token=jwt.token.here");
            assertThat(cookieHeader).contains("HttpOnly");
            assertThat(cookieHeader).contains("SameSite=Lax");
            assertThat(cookieHeader).contains("Path=/api");
            assertThat(cookieHeader).contains("Max-Age=86400");
        }

        @Test
        @DisplayName("Should throw BadCredentialsException for invalid credentials")
        void shouldThrowForInvalidCredentials() {
            // Given
            LoginRequestDto request = new LoginRequestDto("user@test.com", "wrongPassword");
            HttpServletResponse servletResponse = mock(HttpServletResponse.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // When / Then
            assertThatThrownBy(() -> authService.authenticateAndSetCookie(request, servletResponse))
                    .isInstanceOf(BadCredentialsException.class);

            verify(servletResponse, never()).addHeader(any(), any());
        }
    }

    @Nested
    @DisplayName("clearAuthCookie")
    class ClearAuthCookieTests {

        @Test
        @DisplayName("Should write an expired checkpoint_token cookie (Max-Age=0)")
        void shouldWriteExpiredCookie() {
            // Given
            HttpServletResponse servletResponse = mock(HttpServletResponse.class);

            // When
            authService.clearAuthCookie(servletResponse);

            // Then
            ArgumentCaptor<String> headerNameCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);
            verify(servletResponse).addHeader(headerNameCaptor.capture(), headerValueCaptor.capture());

            assertThat(headerNameCaptor.getValue()).isEqualTo("Set-Cookie");
            String cookieHeader = headerValueCaptor.getValue();
            assertThat(cookieHeader).contains("checkpoint_token=");
            assertThat(cookieHeader).contains("Max-Age=0");
            assertThat(cookieHeader).contains("HttpOnly");
        }
    }

    @Nested
    @DisplayName("register")
    class RegisterTests {

        @Test
        @DisplayName("Should successfully register a new user")
        void shouldRegisterNewUser() {
            // Given
            RegisterRequestDto request = new RegisterRequestDto("newuser", "test@test.com", "password123", "password123");
            Role role = new Role("USER");

            when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
            when(userRepository.existsByPseudo("newuser")).thenReturn(false);
            when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

            // When
            authService.register(request);

            // Then
            verify(userRepository).save(any(com.checkpoint.api.entities.User.class));
        }

        @Test
        @DisplayName("Should create USER role if it does not exist")
        void shouldCreateRoleIfNotFound() {
            RegisterRequestDto request = new RegisterRequestDto("newuser", "test@test.com", "password123", "password123");
            Role newRole = new Role("USER");

            when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
            when(userRepository.existsByPseudo("newuser")).thenReturn(false);
            when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
            when(roleRepository.save(any(Role.class))).thenReturn(newRole);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

            authService.register(request);

            verify(roleRepository).save(any(Role.class));
            verify(userRepository).save(any(com.checkpoint.api.entities.User.class));
        }

        @Test
        @DisplayName("Should throw RegistrationConflictException if email exists")
        void shouldThrowIfEmailExists() {
            RegisterRequestDto request = new RegisterRequestDto("newuser", "test@test.com", "password123", "password123");
            when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(RegistrationConflictException.class)
                    .hasMessageContaining("Email is already in use");
        }

        @Test
        @DisplayName("Should throw RegistrationConflictException if pseudo exists")
        void shouldThrowIfPseudoExists() {
            RegisterRequestDto request = new RegisterRequestDto("newuser", "test@test.com", "password123", "password123");
            when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
            when(userRepository.existsByPseudo("newuser")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(RegistrationConflictException.class)
                    .hasMessageContaining("Pseudo is already in use");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException if passwords do not match")
        void shouldThrowIfPasswordsMismatch() {
            RegisterRequestDto request = new RegisterRequestDto("newuser", "test@test.com", "password123", "different123");

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Passwords do not match");

            verify(userRepository, never()).existsByEmail(any());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getCurrentUser")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Should return UserMeDto with role for existing user")
        void shouldReturnUserMeDto() {
            // Given
            UUID userId = UUID.randomUUID();
            Role role = new Role("ADMIN");
            com.checkpoint.api.entities.User user = new com.checkpoint.api.entities.User();
            user.setId(userId);
            user.setPseudo("alice");
            user.setEmail("alice@test.com");
            user.setRole(role);

            when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));

            // When
            UserMeDto result = authService.getCurrentUser("alice@test.com");

            // Then
            assertThat(result.id()).isEqualTo(userId);
            assertThat(result.username()).isEqualTo("alice");
            assertThat(result.email()).isEqualTo("alice@test.com");
            assertThat(result.role()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("Should default role to USER when user has no role")
        void shouldDefaultRoleToUser() {
            // Given
            UUID userId = UUID.randomUUID();
            com.checkpoint.api.entities.User user = new com.checkpoint.api.entities.User();
            user.setId(userId);
            user.setPseudo("bob");
            user.setEmail("bob@test.com");
            user.setRole(null);

            when(userRepository.findByEmail("bob@test.com")).thenReturn(Optional.of(user));

            // When
            UserMeDto result = authService.getCurrentUser("bob@test.com");

            // Then
            assertThat(result.role()).isEqualTo("USER");
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            // Given
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> authService.getCurrentUser("unknown@test.com"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("unknown@test.com");
        }
    }

    @Nested
    @DisplayName("forgotPassword")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Should generate token and send reset email if email exists")
        void shouldGenerateToken() {
            // Given
            com.checkpoint.api.entities.User user = new com.checkpoint.api.entities.User();
            UUID userId = UUID.randomUUID();
            user.setId(userId);
            user.setEmail("user@test.com");

            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

            ForgotPasswordRequestDto request = new ForgotPasswordRequestDto("user@test.com");

            // When
            authService.forgotPassword(request);

            // Then
            verify(passwordResetTokenRepository).deleteByUserId(userId);
            verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
            verify(emailService).sendPasswordResetEmail(any(String.class), any(String.class));
        }

        @Test
        @DisplayName("Should silently ignore if email does not exist")
        void shouldIgnoreIfEmailNotFound() {
            // Given
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            ForgotPasswordRequestDto request = new ForgotPasswordRequestDto("unknown@test.com");

            // When
            authService.forgotPassword(request);

            // Then
            verify(passwordResetTokenRepository, never()).deleteByUserId(any());
            verify(passwordResetTokenRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should reset password and delete token on success")
        void shouldResetPassword() {
            // Given
            com.checkpoint.api.entities.User user = new com.checkpoint.api.entities.User();
            PasswordResetToken token = new PasswordResetToken("valid-token", user, LocalDateTime.now().plusMinutes(15));

            when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
            when(passwordEncoder.encode("new-password123")).thenReturn("encoded-new-password");

            ResetPasswordRequestDto request = new ResetPasswordRequestDto("valid-token", "new-password123");

            // When
            authService.resetPassword(request);

            // Then
            verify(passwordEncoder).encode("new-password123");
            verify(userRepository).save(user);
            verify(passwordResetTokenRepository).delete(token);
        }

        @Test
        @DisplayName("Should throw if token not found")
        void shouldThrowIfTokenNotFound() {
            // Given
            when(passwordResetTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

            ResetPasswordRequestDto request = new ResetPasswordRequestDto("invalid-token", "new-password123");

            // When / Then
            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("Invalid reset token");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw if token expired")
        void shouldThrowIfTokenExpired() {
            // Given
            com.checkpoint.api.entities.User user = new com.checkpoint.api.entities.User();
            PasswordResetToken token = new PasswordResetToken("expired-token", user, LocalDateTime.now().minusMinutes(1));

            when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

            ResetPasswordRequestDto request = new ResetPasswordRequestDto("expired-token", "new-password123");

            // When / Then
            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("Token has expired");

            verify(passwordResetTokenRepository).delete(token);
            verify(userRepository, never()).save(any());
        }
    }
}
