package com.pridebank.token.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.solab.iso8583.IsoMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class IsoToJsonConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String convert(IsoMessage isoMessage) throws Exception {
        ObjectNode json = objectMapper.createObjectNode();
        json.put("messageType", String.format("%04d", isoMessage.getType()));
        if (isoMessage.hasField(2)) {
            String pan = isoMessage.getObjectValue(2).toString();
            json.put("cardNumber", maskPan(pan));
            json.put("accountNumber", pan);
        }
        if (isoMessage.hasField(3)) {
            json.put("processingCode", isoMessage.getObjectValue(3).toString());
        }
        if (isoMessage.hasField(4)) {
            String amountStr = isoMessage.getObjectValue(4).toString();
            json.put("amount", parseAmount(amountStr).toString());
            json.put("amountMinor", amountStr);
        }
        if (isoMessage.hasField(7)) {
            json.put("transmissionDateTime", isoMessage.getObjectValue(7).toString());
        }
        if (isoMessage.hasField(11)) {
            json.put("stan", isoMessage.getObjectValue(11).toString());
        }
        if (isoMessage.hasField(41)) {
            json.put("terminalId", isoMessage.getObjectValue(41).toString().trim());
        }
        if (isoMessage.hasField(49)) {
            json.put("currencyCode", isoMessage.getObjectValue(49).toString());
        }

        return objectMapper.writeValueAsString(json);
    }

    private BigDecimal parseAmount(String amountStr) {
        long cents = Long.parseLong(amountStr.trim());
        return BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private String maskPan(String pan) {
        if (pan == null || pan.length() < 13) {
            return "****";
        }
        return pan.substring(0, 6) + "******" + pan.substring(pan.length() - 4);
    }
}