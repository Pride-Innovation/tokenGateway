package com.pridebank.token.controller;

import com.pridebank.token.dto.*;
import com.pridebank.token.exception.AuthenticationFailedException;
import com.pridebank.token.security.JwtTokenProvider;
import com.pridebank.token.service.ESBAuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ESBAuthenticationService esbAuthService;
    private final JwtTokenProvider tokenProvider;

    /**
     * Login endpoint - Authenticates with ESB and returns JWT token
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        log.info("Login attempt for user: {}", authRequest.getUsername());

        try {
            // Authenticate with ESB platform
            boolean isAuthenticated = esbAuthService.authenticateWithESB(
                    authRequest.getUsername(),
                    authRequest.getPassword()
            );

            if (!isAuthenticated) {
                log.warn("Authentication failed for user: {}", authRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.builder()
                                .message("Invalid credentials")
                                .build());
            }

            // Generate JWT token with embedded credentials
            String token = tokenProvider.generateToken(
                    authRequest.getUsername(),
                    authRequest.getPassword()
            );

            log.info("Authentication successful for user: {}", authRequest.getUsername());

            return ResponseEntity.ok(AuthResponse.builder()
                    .authToken(token)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getExpirationMs())
                    .message("Authentication successful")
                    .build());

        } catch (AuthenticationFailedException ex) {
            log.error("Authentication failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(AuthResponse.builder()
                            .message(ex.getMessage())
                            .build());
        } catch (Exception ex) {
            log.error("Unexpected error during authentication", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder()
                            .message("An unexpected error occurred")
                            .build());
        }
    }


    /**
     * OAuth-style Login endpoint - Authenticates with ESB using form-urlencoded
     * POST /api/auth/login/xml
     * Content-Type: application/x-www-form-urlencoded
     * <p>
     * Accepts:
     * - grant_type: The type of grant being requested
     * - client_id: The client ID (used as username)
     * - client_secret: The client secret (used as password)
     */
    @PostMapping(
            value = "/login/xml",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthResponse> loginWithFormData(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret
    ) {
        log.info("OAuth-style login attempt for client_id: {} with grant_type: {}", clientId, grantType);

        System.out.println("user name::" + clientId);
        System.out.println("password::" + clientSecret);

        try {
            // Validate grant_type
            if (grantType == null || grantType.trim().isEmpty()) {
                log.warn("Missing grant_type parameter");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AuthResponse.builder()
                                .message("grant_type is required")
                                .build());
            }

            // Validate client_id
            if (clientId == null || clientId.trim().isEmpty()) {
                log.warn("Missing client_id parameter");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AuthResponse.builder()
                                .message("client_id is required")
                                .build());
            }

            // Validate client_secret
            if (clientSecret == null || clientSecret.trim().isEmpty()) {
                log.warn("Missing client_secret parameter");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AuthResponse.builder()
                                .message("client_secret is required")
                                .build());
            }

            // Use client_id as username and client_secret as password

            log.debug("Attempting ESB authentication for client: {}", clientId);

            // Authenticate with ESB platform
            boolean isAuthenticated = esbAuthService.authenticateWithESB(clientId, clientSecret);

            if (!isAuthenticated) {
                log.warn("Authentication failed for client_id: {}", clientId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.builder()
                                .message("Invalid client credentials")
                                .build());
            }

            // Generate JWT token with embedded credentials
            String token = tokenProvider.generateToken(clientId, clientSecret);

            log.info("OAuth-style authentication successful for client_id: {}", clientId);

            return ResponseEntity.ok(AuthResponse.builder()
                    .authToken(token)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getExpirationMs())
                    .message("Authentication successful")
                    .build());

        } catch (AuthenticationFailedException ex) {
            log.error("OAuth authentication failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(AuthResponse.builder()
                            .message(ex.getMessage())
                            .build());
        } catch (Exception ex) {
            log.error("Unexpected error during OAuth authentication", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder()
                            .message("An unexpected error occurred")
                            .build());
        }
    }


    /**
     * Verify account number endpoint
     * POST /api/auth/verify-account
     */
    @PostMapping("/verify-account")
    public ResponseEntity<?> verifyAccountNumber(@Valid @RequestBody VerifyAccountRequest request) {
        log.info("Account verification request for account: {}", request.getAccountNumber());

        try {
            // Validate token and extract credentials
            TokenValidationResult validationResult = validateAndExtractCredentials(request.getAuthToken());

            if (!validationResult.valid()) {
                return validationResult.errorResponse();
            }

            String username = validationResult.username();
            String password = validationResult.password();

            log.debug("Extracted credentials from token for user: {}", username);

            // Call ESB to validate account
            ResponseEntity<AccountValidationResponse> esbResponse =
                    esbAuthService.validateAccountNumber(username, password, request.getAccountNumber());

            // Return ESB response
            if (esbResponse.getStatusCode().is2xxSuccessful()) {
                log.info("Account verification successful for account: {}", request.getAccountNumber());
                return new ResponseEntity<>(
                        esbResponse.getBody(),
                        HttpStatusCode.valueOf(Objects.requireNonNull(esbResponse.getBody()).getCustomerId().isBlank() ? 400 : 200)
                );
            } else {
                log.warn("Account verification failed for account: {} with status: {}",
                        request.getAccountNumber(), esbResponse.getStatusCode());
                return ResponseEntity.status(esbResponse.getStatusCode())
                        .body(esbResponse.getBody());
            }

        } catch (IllegalArgumentException ex) {
            log.error("Invalid token format: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AccountValidationResponse.builder()
                            .response("Invalid token format: " + ex.getMessage())
                            .build());
        } catch (Exception ex) {
            log.error("Account verification failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AccountValidationResponse.builder()
                            .response("Failed to verify account: " + ex.getMessage())
                            .build());
        }
    }

    /**
     * Deposit/Charge endpoint
     * POST /api/auth/charge
     */
    @PostMapping("/charge")
    public ResponseEntity<?> charge(@Valid @RequestBody ChargeRequest request) {
        log.info("Deposit/charge request received");

        try {
            System.out.println("We are inside the charge method:: " + request.getAuthToken());
            // Validate token and extract credentials
            TokenValidationResult validationResult = validateAndExtractCredentials(request.getAuthToken());

            if (!validationResult.valid()) {
                return validationResult.errorResponse();
            }

            String username = validationResult.username();
            String password = validationResult.password();

            log.debug("Processing deposit for user: {}", username);

            ResponseEntity<ChargeResponse> esbResponse =
                    esbAuthService.chargeCard(username, password, request);

            Map<String, Object> response = new HashMap<>();
            response.put("code", Objects.requireNonNull(esbResponse.getBody()).getCode());
            response.put("description", esbResponse.getBody().getDescription());
            response.put("transaction_id", esbResponse.getBody().getTransaction_id());
            response.put("FT", esbResponse.getBody().getFt());

            return new ResponseEntity<>(
                    response,
                    HttpStatusCode.valueOf(Objects.equals(esbResponse.getBody().getCode(), "200") ? 200 : 400)
            );

        } catch (Exception ex) {
            log.error("Deposit processing failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process deposit: " + ex.getMessage());
        }
    }

    /**
     * Validate token endpoint
     * GET /api/auth/validate
     */
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (!authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid authorization header format");
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix

            boolean isValid = tokenProvider.validateToken(token);
            boolean isExpired = tokenProvider.isTokenExpired(token);

            if (isValid && !isExpired) {
                String username = tokenProvider.getUsernameFromToken(token);
                return ResponseEntity.ok()
                        .body(String.format("Token is valid for user: %s", username));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Token is invalid or expired");
            }
        } catch (Exception ex) {
            log.error("Token validation error", ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token format");
        }
    }

    /**
     * Health check endpoint
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Token Gateway is running");
    }

    // ==================== PRIVATE METHODS ====================

    /**
     * Private method to validate token and extract credentials
     * Reusable across multiple endpoints
     *
     * @param token The JWT token to validate
     * @return TokenValidationResult containing validation status and credentials
     */
    private TokenValidationResult validateAndExtractCredentials(String token) {
        // 1. Validate token
        if (!tokenProvider.validateToken(token)) {
            log.warn("Invalid token provided");
            return TokenValidationResult.invalid(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(AccountValidationResponse.builder()
                                    .response("Invalid authentication token")
                                    .build())
            );
        }

        // 2. Check if token is expired
        if (tokenProvider.isTokenExpired(token)) {
            log.warn("Expired token provided");
            return TokenValidationResult.invalid(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(AccountValidationResponse.builder()
                                    .response("Authentication token has expired")
                                    .build())
            );
        }

        // 3. Extract credentials from token
        try {
            String[] credentials = tokenProvider.getDecodedCredentialsFromToken(token);
            String username = credentials[0];
            String password = credentials[1];

            log.debug("Successfully extracted credentials from token for user: {}", username);

            return TokenValidationResult.valid(username, password);

        } catch (Exception ex) {
            log.error("Failed to extract credentials from token", ex);
            return TokenValidationResult.invalid(
                    ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(AccountValidationResponse.builder()
                                    .response("Failed to extract credentials from token")
                                    .build())
            );
        }
    }

    // ==================== INNER RECORD ====================

    /**
     * Inner Record to hold token validation result
     */
    private record TokenValidationResult(boolean valid, String username, String password,
                                         ResponseEntity<?> errorResponse) {

        public static TokenValidationResult valid(String username, String password) {
            return new TokenValidationResult(true, username, password, null);
        }

        public static TokenValidationResult invalid(ResponseEntity<?> errorResponse) {
            return new TokenValidationResult(false, null, null, errorResponse);
        }

    }
}