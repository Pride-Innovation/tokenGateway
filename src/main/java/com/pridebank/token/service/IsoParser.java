package com.pridebank.token.service;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

@Service
@Slf4j
public class IsoParser {

    @Autowired
    private MessageFactory<IsoMessage> messageFactory;

    public IsoMessage parse(byte[] response) throws ParseException, UnsupportedEncodingException {
        if (response == null || response.length == 0) {
            throw new ParseException("Empty ISO message received", 0);
        }

        return messageFactory.parseMessage(response, 0);
    }
}