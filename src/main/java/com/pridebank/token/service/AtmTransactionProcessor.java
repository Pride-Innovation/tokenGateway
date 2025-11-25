package com.pridebank.token.service;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AtmTransactionProcessor {

    @Autowired
    private IsoToJsonConverter isoToJsonConverter;

    @Autowired
    private JsonToIsoConverter jsonToIsoConverter;

    @Autowired
    private EsbGatewayService esbGatewayService;

    @Autowired
    private MessageFactory<IsoMessage> messageFactory;

    @Autowired
    private IsoMessageBuilder isoMessageBuilder;

    public IsoMessage processTransaction(IsoMessage isoRequest) {
        String stan = isoRequest.hasField(11) ? isoRequest.getObjectValue(11).toString() : "unknown";

        try {
            String jsonRequest = isoToJsonConverter.convert(isoRequest);
            String jsonResponse = esbGatewayService.sendToEsb(jsonRequest, isoRequest);
            return jsonToIsoConverter.convert(jsonResponse, isoRequest);

        } catch (Exception e) {
            log.error("Transaction failed - STAN: {}", stan, e);
            return createErrorResponse(isoRequest, "96", "System error");
        }
    }

    public IsoMessage createErrorResponse(IsoMessage request, String responseCode, String message) {
        try {
            int responseMti;
            IsoMessage response;

            if (request != null) {
                responseMti = request.getType() + 10; // e.g., 0200 -> 0210
                response = isoMessageBuilder.createResponseFromRequest(request, responseMti);
            } else {
                responseMti = 0x0210;
                response = messageFactory.newMessage(responseMti);
            }

            // Field 39: Response code (2 chars)
            String code = (responseCode == null || responseCode.isBlank()) ? "96" : responseCode;
            response.setValue(39, code, IsoType.ALPHA, 2);

            // Optional: Field 44 message (LLVAR, up to 25 chars here)
            if (message != null && !message.isBlank()) {
                String msg = message.length() > 25 ? message.substring(0, 25) : message;
                response.setValue(44, msg, IsoType.LLVAR, msg.length());
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Unable to create error response", e);
        }
    }
}