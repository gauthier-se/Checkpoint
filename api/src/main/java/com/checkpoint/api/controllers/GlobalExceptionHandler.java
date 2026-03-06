package com.checkpoint.api.controllers;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.checkpoint.api.exceptions.ExternalApiUnavailableException;
import com.checkpoint.api.exceptions.IgdbApiException;
import com.checkpoint.api.exceptions.ExternalGameNotFoundException;
import com.checkpoint.api.exceptions.GameAlreadyInLibraryException;
import com.checkpoint.api.exceptions.GameNotFoundException;
import com.checkpoint.api.exceptions.GameNotInLibraryException;
import com.checkpoint.api.exceptions.GameAlreadyInBacklogException;
import com.checkpoint.api.exceptions.GameAlreadyInWishlistException;
import com.checkpoint.api.exceptions.GameNotInBacklogException;
import com.checkpoint.api.exceptions.GameNotInWishlistException;
import com.checkpoint.api.exceptions.InvalidTokenException;
import com.checkpoint.api.exceptions.RegistrationConflictException;
import com.checkpoint.api.exceptions.PlayLogNotFoundException;
import com.checkpoint.api.exceptions.RateNotFoundException;

/**
 * Global exception handler for REST controllers.
 * Provides consistent error responses across all endpoints.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles GameNotFoundException.
     *
     * @param ex the exception
     * @return error response with 404 status
     */
    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGameNotFound(GameNotFoundException ex) {
        log.warn("Game not found: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles ExternalGameNotFoundException when a game is not found in IGDB.
     *
     * @param ex the exception
     * @return error response with 404 status
     */
    @ExceptionHandler(ExternalGameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleExternalGameNotFound(ExternalGameNotFoundException ex) {
        log.warn("External game not found: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles ExternalApiUnavailableException when IGDB API is down or rate limited.
     *
     * @param ex the exception
     * @return error response with 503 status
     */
    @ExceptionHandler(ExternalApiUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiUnavailable(ExternalApiUnavailableException ex) {
        log.error("External API unavailable: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * Handles IgdbApiException as a safety net when the exception is not caught
     * and re-wrapped by a service layer.
     *
     * @param ex the exception
     * @return error response with 503 status
     */
    @ExceptionHandler(IgdbApiException.class)
    public ResponseEntity<ErrorResponse> handleIgdbApiException(IgdbApiException ex) {
        log.error("IGDB API error: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * Handles GameAlreadyInLibraryException when a game is already in the user's library.
     *
     * @param ex the exception
     * @return error response with 409 status
     */
    @ExceptionHandler(GameAlreadyInLibraryException.class)
    public ResponseEntity<ErrorResponse> handleGameAlreadyInLibrary(GameAlreadyInLibraryException ex) {
        log.warn("Game already in library: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handles GameNotInLibraryException when a game is not found in the user's library.
     *
     * @param ex the exception
     * @return error response with 404 status
     */
    @ExceptionHandler(GameNotInLibraryException.class)
    public ResponseEntity<ErrorResponse> handleGameNotInLibrary(GameNotInLibraryException ex) {
        log.warn("Game not in library: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles GameAlreadyInWishlistException when a game is already in the user's wishlist.
     *
     * @param ex the exception
     * @return error response with 409 status
     */
    @ExceptionHandler(GameAlreadyInWishlistException.class)
    public ResponseEntity<ErrorResponse> handleGameAlreadyInWishlist(GameAlreadyInWishlistException ex) {
        log.warn("Game already in wishlist: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handles GameNotInWishlistException when a game is not found in the user's wishlist.
     *
     * @param ex the exception
     * @return error response with 404 status
     */
    @ExceptionHandler(GameNotInWishlistException.class)
    public ResponseEntity<ErrorResponse> handleGameNotInWishlist(GameNotInWishlistException ex) {
        log.warn("Game not in wishlist: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles GameAlreadyInBacklogException when a game is already in the user's backlog.
     *
     * @param ex the exception
     * @return error response with 409 status
     */
    @ExceptionHandler(GameAlreadyInBacklogException.class)
    public ResponseEntity<ErrorResponse> handleGameAlreadyInBacklog(GameAlreadyInBacklogException ex) {
        log.warn("Game already in backlog: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handles GameNotInBacklogException when a game is not found in the user's backlog.
     *
     * @param ex the exception
     * @return error response with 404 status
     */
    @ExceptionHandler(GameNotInBacklogException.class)
    public ResponseEntity<ErrorResponse> handleGameNotInBacklog(GameNotInBacklogException ex) {
        log.warn("Game not in backlog: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles PlayLogNotFoundException when a play log is not found.
     *
     * @param ex the exception
     * @return error response with 404 status
     */
    @ExceptionHandler(PlayLogNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlayLogNotFound(PlayLogNotFoundException ex) {
        log.warn("Play log not found: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    /**
     * Handles RateNotFoundException when a rating is not found for a user and game.
     *
     * @param ex the exception
     * @return error response with 404 status
     */
    @ExceptionHandler(RateNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRateNotFound(RateNotFoundException ex) {
        log.warn("Rating not found: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles missing required request parameters.
     *
     * @param ex the exception
     * @return error response with 400 status
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        log.warn("Missing parameter: {}", message);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles validation errors from @Valid annotated request bodies.
     *
     * @param ex the exception
     * @return error response with 400 status containing the first validation error
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        log.warn("Validation error: {}", message);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles IllegalArgumentException for bad requests.
     *
     * @param ex the exception
     * @return error response with 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles RegistrationConflictException when a registration attempt uses a
     * duplicate email or pseudo.
     *
     * @param ex the exception
     * @return error response with 409 status
     */
    @ExceptionHandler(RegistrationConflictException.class)
    public ResponseEntity<ErrorResponse> handleRegistrationConflict(RegistrationConflictException ex) {
        log.warn("Registration conflict: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handles InvalidTokenException when a password reset token is invalid or expired.
     *
     * @param ex the exception
     * @return error response with 400 status
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn("Invalid token: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handles access denied (insufficient privileges).
     *
     * @param ex the exception
     * @return error response with 403 status
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "You do not have permission to access this resource",
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handles type mismatches for path variables and request parameters (e.g., String to UUID).
     *
     * @param ex the exception
     * @return error response with 400 status
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Type mismatch for parameter '%s'. Failed to convert value to required type.", ex.getName());
        log.warn("Type mismatch error: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles all other exceptions.
     *
     * @param ex the exception
     * @return error response with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Standard error response format.
     */
    public record ErrorResponse(
            int status,
            String error,
            String message,
            LocalDateTime timestamp
    ) {}
}
