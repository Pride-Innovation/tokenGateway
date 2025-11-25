package com.pridebank.token.service;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import com.pridebank.token.validation.IsoValidator;
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

    @Autowired
    private IsoValidator isoValidator;

    public IsoMessage processTransaction(IsoMessage isoRequest) {
        String stan = (isoRequest != null && isoRequest.hasField(11)) ?
                isoRequest.getObjectValue(11).toString() : "unknown";

        // Validate request first
        IsoValidator.ValidationResult vr = isoValidator.validate0200(isoRequest);
        if (!vr.isValid()) {
            log.warn("Validation failed - STAN: {} - {}", stan, vr.summary());
            // 30 = Format error
            return createErrorResponse(isoRequest, "30", truncate(vr.summary()));
        }

        try {
            assert isoRequest != null;
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

            String code = (responseCode == null || responseCode.isBlank()) ? "96" : responseCode;
            response.setValue(39, code, IsoType.ALPHA, 2);

            if (message != null && !message.isBlank()) {
                String msg = truncate(message);
                response.setValue(44, msg, IsoType.LLVAR, msg.length());
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Unable to create error response", e);
        }
    }

    private String truncate(String s) {
        return s.length() > 25 ? s.substring(0, 25) : s;
    }
}