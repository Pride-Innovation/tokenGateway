package com.pridebank.token.service;

import com.pridebank.token.client.ESBAuthResponse;
import com.pridebank.token.client.ESBClient;
import com.pridebank.token.dto.AccountValidationResponse;
import com.pridebank.token.dto.ChargeRequest;
import com.pridebank.token.dto.ChargeResponse;
import com.pridebank.token.exception.AuthenticationFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class ESBAuthenticationService {

    private final ESBClient esbClient;

    /**
     * Authenticate credentials against ESB platform using OpenFeign
     */
    public boolean authenticateWithESB(String username, String password) {
        try {
            String authHeader = createBasicAuthHeader(username, password);

            log.debug("Authenticating with ESB for user: {}", username);

            ResponseEntity<ESBAuthResponse> response = esbClient.authenticate(authHeader);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                boolean isAuthenticated = response.getBody().isAuthenticated();

                log.info("ESB authentication result for user {}: {}", username, isAuthenticated);
                log.debug("ESB response body: {}", response.getBody());

                return isAuthenticated;
            }


            log.warn("ESB returned unsuccessful status: {}", response.getStatusCode());
            return false;

        } catch (AuthenticationFailedException ex) {
            log.warn("ESB authentication failed for user {}: {}", username, ex.getMessage());
            return false;
        } catch (Exception ex) {
            log.error("ESB authentication failed for user {}", username, ex);
            throw new AuthenticationFailedException(
                    "Failed to connect to ESB platform: " + ex.getMessage()
            );
        }
    }

    /**
     * Validate account number with ESB
     *
     * @param username      The authenticated username from token
     * @param password      The authenticated password from token
     * @param accountNumber The account number to validate
     * @return AccountValidationResponse containing account details or error
     */
    public ResponseEntity<AccountValidationResponse> validateAccountNumber(
            String username,
            String password,
            String accountNumber) {
        try {
            String authHeader = createBasicAuthHeader(username, password);

            log.info("Validating account number {} for user: {}", accountNumber, username);

            ResponseEntity<AccountValidationResponse> response = esbClient.validateAccount(
                    authHeader,
                    accountNumber
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Account validation successful for account: {}", accountNumber);
                log.debug("Account validation response: {}", response.getBody());
            } else {
                log.warn("Account validation returned status: {}", response.getStatusCode());
            }

            return response;

        } catch (Exception ex) {
            log.error("Failed to validate account {} for user {}", accountNumber, username, ex);
            throw new RuntimeException(
                    "Failed to validate account with ESB: " + ex.getMessage()
            );
        }
    }

    public ResponseEntity<ChargeResponse> chargeCard(
            String username,
            String password,
            ChargeRequest request
    ) {
        try {
            String authHeader = createBasicAuthHeader(username, password);

            log.info("Validating Debit account number {} for user: {}", request.getDebit_account(), username);

            ResponseEntity<ChargeResponse> response = esbClient.CardChargePostRequest(
                    authHeader,
                    request
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Account charging successful for account: {}", request.getDebit_account());
                log.debug("Account charging response: {}", response.getBody());
            } else {
                log.warn("Account charging returned status: {}", response.getStatusCode());
            }

            return response;
        } catch (Exception ex) {
            log.error("Failed to charge account {} for card {}", request.getDebit_account(), username, ex);
            throw new RuntimeException(
                    "Failed to validate account with ESB: " + ex.getMessage()
            );
        }
    }

    /**
     * Create Basic Auth header
     */
    private String createBasicAuthHeader(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(
                auth.getBytes(StandardCharsets.UTF_8)
        );
        String basicAuth = "Basic " + new String(encodedAuth);

        log.trace("Created Basic Auth header for user: {}", username);

        return basicAuth;
    }
}