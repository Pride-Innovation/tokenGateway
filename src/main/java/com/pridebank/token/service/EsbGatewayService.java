package com.pridebank.token.service;

import com.pridebank.token.client.ESBClient;
import com.pridebank.token.dto.AtmTransactionRequest;
import com.pridebank.token.dto.AtmTransactionResponse;
import com.solab.iso8583.IsoMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Slf4j
public class EsbGatewayService {

    @Autowired
    private ESBClient esbClient;

    @Value("${esb.atm.username}")
    private String atmUsername;

    @Value("${esb.atm.password}")
    private String atmPassword;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String sendToEsb(String jsonRequest, IsoMessage isoMessage) {
        try {
            String authHeader = createBasicAuthHeader(atmUsername, atmPassword);
            AtmTransactionRequest request = objectMapper.readValue(jsonRequest, AtmTransactionRequest.class);

            ResponseEntity<?> response = esbClient.CardChargePostRequest(authHeader, request);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return createErrorResponse("SYSTEM_ERROR", "ESB communication failed");
            }

            return objectMapper.writeValueAsString(response.getBody());

        } catch (Exception e) {
            log.error("ESB communication failed", e);
            return createErrorResponse("SYSTEM_ERROR", e.getMessage());
        }
    }

    private String createBasicAuthHeader(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }

    private String createErrorResponse(String code, String message) {
        try {
            AtmTransactionResponse errorResponse = AtmTransactionResponse.builder()
                    .responseCode(code)
                    .message(message)
                    .build();
            return objectMapper.writeValueAsString(errorResponse);
        } catch (Exception e) {
            return "{\"responseCode\":\"SYSTEM_ERROR\",\"message\":\"Unknown error\"}";
        }
    }
}