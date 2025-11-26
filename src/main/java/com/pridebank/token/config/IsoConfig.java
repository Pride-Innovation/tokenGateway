package com.pridebank.token.config;

import com.solab.iso8583.*;
import com.solab.iso8583.parse.*;
import com.solab.iso8583.parse.date.Date10ParseInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class IsoConfig {

    /**
     * Base template for all messages (0200, 0210, 0231).
     */
    private IsoMessage base(int mti) {
        IsoMessage m = new IsoMessage();
        m.setType(mti);

        m.setField(2, new IsoValue<>(IsoType.LLVAR, ""));
        m.setField(3, new IsoValue<>(IsoType.NUMERIC, "000000", 6));
        m.setField(4, new IsoValue<>(IsoType.NUMERIC, "000000000000", 12));
        m.setField(7, new IsoValue<>(IsoType.DATE10, new java.util.Date(), 10));
        m.setField(11, new IsoValue<>(IsoType.NUMERIC, "000000", 6));
        m.setField(12, new IsoValue<>(IsoType.NUMERIC, "000000", 6));
        m.setField(13, new IsoValue<>(IsoType.NUMERIC, "0000", 4));

        // Only add field 38 for 0x210 (success response)
        if (mti == 0x210) {
            m.setField(38, new IsoValue<>(IsoType.ALPHA, "      ", 6));
        }

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

    /**
     * PARSE MAP FOR 0200
     */
    private java.util.Map<Integer, FieldParseInfo> parseMap0200() {
        var map = new java.util.HashMap<Integer, FieldParseInfo>();

        map.put(2, new LlvarParseInfo());
        map.put(3, new NumericParseInfo(6));
        map.put(4, new NumericParseInfo(12));
        map.put(7, new Date10ParseInfo());
        map.put(11, new NumericParseInfo(6));
        map.put(12, new NumericParseInfo(6));
        map.put(13, new NumericParseInfo(4));
        map.put(38, new AlphaParseInfo(6));
        map.put(39, new AlphaParseInfo(2));
        map.put(41, new AlphaParseInfo(8));
        map.put(44, new LlvarParseInfo());
        map.put(49, new NumericParseInfo(3));
        map.put(54, new LllvarParseInfo());
        map.put(55, new LllvarParseInfo());
        map.put(60, new LllvarParseInfo());
        map.put(61, new LllvarParseInfo());
        map.put(62, new LllvarParseInfo());
        map.put(63, new LllvarParseInfo());
        map.put(64, new BinaryParseInfo(8));
        map.put(70, new NumericParseInfo(3));

        return map;
    }

    /**
     * PARSE MAP FOR 0210
     */
    private java.util.Map<Integer, FieldParseInfo> parseMap0210() {
        var map = new java.util.HashMap<Integer, FieldParseInfo>();

        map.put(2, new LlvarParseInfo());
        map.put(3, new NumericParseInfo(6));
        map.put(4, new NumericParseInfo(12));
        map.put(7, new Date10ParseInfo());
        map.put(11, new NumericParseInfo(6));
        map.put(12, new NumericParseInfo(6));
        map.put(13, new NumericParseInfo(4));
        map.put(38, new AlphaParseInfo(6));
        map.put(39, new AlphaParseInfo(2));
        map.put(41, new AlphaParseInfo(8));
        map.put(44, new LlvarParseInfo());
        map.put(49, new NumericParseInfo(3));
        map.put(54, new LllvarParseInfo());
        map.put(55, new LllvarParseInfo());
        map.put(60, new LllvarParseInfo());
        map.put(61, new LllvarParseInfo());
        map.put(62, new LllvarParseInfo());
        map.put(63, new LllvarParseInfo());
        map.put(64, new BinaryParseInfo(8));
        map.put(70, new NumericParseInfo(3));

        return map;
    }

    /**
     * PARSE MAP FOR 0231 (error/validation failure response)
     */
    private java.util.Map<Integer, FieldParseInfo> parseMap0231() {
        var map = new java.util.HashMap<Integer, FieldParseInfo>();

        map.put(2, new LlvarParseInfo());
        map.put(3, new NumericParseInfo(6));
        map.put(4, new NumericParseInfo(12));
        map.put(7, new Date10ParseInfo());
        map.put(11, new NumericParseInfo(6));
        map.put(12, new NumericParseInfo(6));
        map.put(13, new NumericParseInfo(4));
        map.put(38, new AlphaParseInfo(6));  // Add field 38 parser to handle if present
        map.put(39, new AlphaParseInfo(2));
        map.put(41, new AlphaParseInfo(8));
        map.put(44, new LlvarParseInfo());  // Error message text
        map.put(49, new NumericParseInfo(3));
        map.put(54, new LllvarParseInfo());
        map.put(55, new LllvarParseInfo());
        map.put(60, new LllvarParseInfo());
        map.put(61, new LllvarParseInfo());
        map.put(62, new LllvarParseInfo());
        map.put(63, new LllvarParseInfo());
        map.put(64, new BinaryParseInfo(8));
        map.put(70, new NumericParseInfo(3));

        return map;
    }

    @Bean
    public MessageFactory<IsoMessage> messageFactory() {
        log.info("Initializing MessageFactory programmatically (no XML)");

        MessageFactory<IsoMessage> f = new MessageFactory<>();
        f.setCharacterEncoding("UTF-8");
        f.setAssignDate(false);
        f.setUseBinaryBitmap(true);
        f.setIgnoreLastMissingField(true);

        f.addMessageTemplate(base(0x200));
        f.addMessageTemplate(base(0x210));
        f.addMessageTemplate(base(0x0231));  // Add error response template

        f.setParseMap(0x200, parseMap0200());
        f.setParseMap(0x210, parseMap0210());
        f.setParseMap(0x0231, parseMap0231());  // Add error response parse map

        log.info("✓ Programmatic MessageFactory ready");
        return f;
    }
}