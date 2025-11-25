package com.pridebank.token.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.pridebank.token.util.ResponseCodeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class JsonToIsoConverter {

    @Autowired
    private IsoMessageBuilder isoMessageBuilder;

    @Autowired
    private ResponseCodeMapper responseCodeMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public IsoMessage convert(String jsonResponse, IsoMessage originalRequest) throws Exception {
        JsonNode json = objectMapper.readTree(jsonResponse);
        int responseMti = originalRequest.getType() + 10;
        IsoMessage response = isoMessageBuilder.createResponseFromRequest(originalRequest, responseMti);

        if (json.hasNonNull("authorizationCode")) {
            String authCode = String.format("%-6s", json.get("authorizationCode").asText());
            response.setValue(38, authCode, IsoType.ALPHA, 6);
        }

        String isoCode = responseCodeMapper.mapEsbToIso(
                json.hasNonNull("responseCode") ? json.get("responseCode").asText() : "SYSTEM_ERROR"
        );
        response.setValue(39, isoCode, IsoType.ALPHA, 2);

        if (json.hasNonNull("availableBalance")) {
            String balanceField = formatBalanceAmount(json.get("availableBalance").decimalValue());
            response.setValue(54, balanceField, IsoType.LLLVAR, balanceField.length());
        }

        if (json.hasNonNull("message")) {
            String message = json.get("message").asText();
            if (message.length() > 25) {
                message = message.substring(0, 25);
            }
            response.setValue(44, message, IsoType.LLVAR, message.length());
        }

        return response;
    }

    private String formatBalanceAmount(BigDecimal amount) {
        long cents = amount.multiply(BigDecimal.valueOf(100)).longValue();
        return String.format("%012d", cents);
    }
}