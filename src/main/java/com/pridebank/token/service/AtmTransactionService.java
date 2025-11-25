package com.pridebank.token.service;

import com.solab.iso8583.IsoMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AtmTransactionService
 * ---------------------
 * This service acts as the high-level orchestrator for the end-to-end ISO-8583 transaction flow.
 * <p>
 * Responsibilities:
 * 1. Build a financial ISO-8583 request message (0200) using IsoMessageBuilder
 * 2. Send the packed ISO-8583 message to the remote host using IsoClient
 * 3. Receive the raw ISO-8583 response bytes
 * 4. Parse the response into an IsoMessage object (0210) using IsoParser
 * <p>
 * This class contains **no business logic** and **no ISO field mapping logic**.
 * It merely coordinates the steps required to perform a request/response cycle.
 */
@Service
public class AtmTransactionService {

    @Autowired
    private IsoMessageBuilder builder;

    @Autowired
    private IsoClient client;

    @Autowired
    private IsoParser parser;

    public IsoMessage sendFinancialRequest(String pan, long amount, String terminalId, String processingCode) throws Exception {
        IsoMessage request = builder.build0200(pan, amount, terminalId, processingCode);
        byte[] respBytes = client.send("127.0.0.1", 5000, request);
        return parser.parse(respBytes);
    }
}
