package com.pridebank.token.config;

import com.solab.iso8583.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class IsoConfig {

    private IsoMessage base(int mti) {
        IsoMessage m = new IsoMessage();
        m.setType(mti); // 0x200 -> "0200", 0x210 -> "0210"
        m.setField(2,  new IsoValue<>(IsoType.LLVAR, ""));
        m.setField(3,  new IsoValue<>(IsoType.NUMERIC, "000000", 6));
        m.setField(4,  new IsoValue<>(IsoType.NUMERIC, "000000000000", 12));
        m.setField(7,  new IsoValue<>(IsoType.NUMERIC, "0000000000", 10));
        m.setField(11, new IsoValue<>(IsoType.NUMERIC, "000000", 6));
        m.setField(12, new IsoValue<>(IsoType.NUMERIC, "000000", 6));
        m.setField(13, new IsoValue<>(IsoType.NUMERIC, "0000", 4));
        m.setField(38, new IsoValue<>(IsoType.ALPHA, "      ", 6));
        m.setField(39, new IsoValue<>(IsoType.ALPHA, "00", 2));
        m.setField(41, new IsoValue<>(IsoType.ALPHA, "        ", 8));
        m.setField(44, new IsoValue<>(IsoType.LLVAR, ""));
        m.setField(49, new IsoValue<>(IsoType.NUMERIC, "566", 3));
        m.setField(54, new IsoValue<>(IsoType.LLLVAR, ""));
        m.setField(55, new IsoValue<>(IsoType.LLLVAR, ""));
        m.setField(60, new IsoValue<>(IsoType.LLLVAR, ""));
        m.setField(61, new IsoValue<>(IsoType.LLLVAR, ""));
        m.setField(62, new IsoValue<>(IsoType.LLLVAR, ""));
        m.setField(63, new IsoValue<>(IsoType.LLLVAR, ""));
        m.setField(64, new IsoValue<>(IsoType.BINARY, new byte[8], 8));
        m.setField(70, new IsoValue<>(IsoType.NUMERIC, "000", 3));
        return m;
    }

    @Bean
    public MessageFactory<IsoMessage> messageFactory() {
        log.info("Initializing MessageFactory programmatically (no XML)");
        MessageFactory<IsoMessage> f = new MessageFactory<>();
        f.setCharacterEncoding("UTF-8");
        f.setAssignDate(true);
        f.setUseBinaryBitmap(true);

        f.addMessageTemplate(base(0x200)); // Financial request 0200
        f.addMessageTemplate(base(0x210)); // Response 0210

        log.info("✓ Programmatic MessageFactory ready");
        return f;
    }
}