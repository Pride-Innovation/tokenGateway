package com.pridebank.token.service;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.pridebank.token.util.StanGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class IsoMessageBuilder {

    @Autowired
    private MessageFactory<IsoMessage> messageFactory;

    @Autowired
    private StanGenerator stanGenerator;

    private static final DateTimeFormatter TRANSMISSION_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMddHHmmss");
    private static final DateTimeFormatter LOCAL_TIME_FORMAT =
            DateTimeFormatter.ofPattern("HHmmss");
    private static final DateTimeFormatter LOCAL_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMdd");

    public IsoMessage build0200(String pan, long amount, String terminalId, String processingCode) {
        IsoMessage msg = messageFactory.newMessage(0x200);
        LocalDateTime now = LocalDateTime.now();

        msg.setValue(2, pan, IsoType.LLVAR, pan.length());
        msg.setValue(3, processingCode, IsoType.NUMERIC, 6);
        msg.setValue(4, String.format("%012d", amount), IsoType.NUMERIC, 12);
        msg.setValue(7, now.format(TRANSMISSION_DATE_FORMAT), IsoType.NUMERIC, 10);
        msg.setValue(11, stanGenerator.generateStanForTerminal(terminalId), IsoType.NUMERIC, 6);
        msg.setValue(12, now.format(LOCAL_TIME_FORMAT), IsoType.NUMERIC, 6);
        msg.setValue(13, now.format(LOCAL_DATE_FORMAT), IsoType.NUMERIC, 4);
        msg.setValue(41, String.format("%-8s", terminalId), IsoType.ALPHA, 8);
        msg.setValue(49, "566", IsoType.NUMERIC, 3);

        return msg;
    }

    public IsoMessage createResponseFromRequest(IsoMessage request, int responseMti) {
        IsoMessage response = messageFactory.newMessage(responseMti);
        for (int i = 2; i <= 128; i++) {
            IsoValue<?> field = request.getField(i);
            if (field != null) {
                response.setField(i, field.clone());
            }
        }
        return response;
    }
}