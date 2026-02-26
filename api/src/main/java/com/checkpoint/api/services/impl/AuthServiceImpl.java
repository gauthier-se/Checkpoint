package com.checkpoint.api.services.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import com.checkpoint.api.dto.auth.LoginRequestDto;
import com.checkpoint.api.dto.auth.UserMeDto;
import com.checkpoint.api.entities.User;
import com.checkpoint.api.repositories.UserRepository;
import com.checkpoint.api.security.JwtService;
import com.checkpoint.api.services.AuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

import com.checkpoint.api.dto.auth.ForgotPasswordRequestDto;
import com.checkpoint.api.dto.auth.ResetPasswordRequestDto;
import com.checkpoint.api.entities.PasswordResetToken;
import com.checkpoint.api.repositories.PasswordResetTokenRepository;
import com.checkpoint.api.exceptions.InvalidTokenException;
import com.checkpoint.api.services.EmailService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.checkpoint.api.dto.auth.RegisterRequestDto;
import com.checkpoint.api.entities.Role;
import com.checkpoint.api.repositories.RoleRepository;

/**
 * Implementation of {@link AuthService}.
 * Delegates credential validation to Spring Security's {@link AuthenticationManager},
 * then either generates a JWT token (Desktop) or creates a session (Web).
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           UserDetailsService userDetailsService,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           RoleRepository roleRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }

    @Override
    public String authenticateAndGenerateToken(LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        return jwtService.generateToken(userDetails);
    }

    @Override
    public void authenticateAndCreateSession(LoginRequestDto request,
                                             HttpServletRequest servletRequest,
                                             HttpServletResponse servletResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        HttpSession session = servletRequest.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext
        );
    }

    @Override
    public void logoutSession(HttpServletRequest servletRequest,
                              HttpServletResponse servletResponse) {
        HttpSession session = servletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }

    @Override
    public UserMeDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        String roleName = user.getRole() != null ? user.getRole().getName() : "USER";

        return new UserMeDto(
                user.getId(),
                user.getPseudo(),
                user.getEmail(),
                roleName
        );
    }

    @Override
    public void register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DataIntegrityViolationException("Email is already in use");
        }
        if (userRepository.existsByPseudo(request.pseudo())) {
            throw new DataIntegrityViolationException("Pseudo is already in use");
        }

        Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
            Role newRole = new Role("USER");
            return roleRepository.save(newRole);
        });

        User user = new User(
                request.pseudo(),
                request.email(),
                passwordEncoder.encode(request.password())
        );
        user.setRole(userRole);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDto request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            // Clear old tokens for this user
            passwordResetTokenRepository.deleteByUserId(user.getId());

            // Generate new token valid for 15 minutes
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(
                    token,
                    user,
                    LocalDateTime.now().plusMinutes(15)
            );
            passwordResetTokenRepository.save(resetToken);

            // Send email using EmailService
            String resetLink = "http://localhost:3000/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidTokenException("Token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Invalidate token after use
        passwordResetTokenRepository.delete(resetToken);
    }
}
