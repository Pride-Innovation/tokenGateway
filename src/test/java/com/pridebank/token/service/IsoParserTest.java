package com.pridebank.token.service;

import com.pridebank.token.TestInjection;
import com.pridebank.token.config.IsoConfig;
import com.pridebank.token.util.StanGenerator;
import com.solab.iso8583.IsoMessage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IsoParserTest {

    @Test
    void parseRoundTrip() throws Exception {
        IsoMessageBuilder builder = new IsoMessageBuilder();
        var mf = new IsoConfig().messageFactory();
        TestInjection.set(builder, "messageFactory", mf);
        TestInjection.set(builder, "stanGenerator", new StanGenerator());
        TestInjection.set(builder, "clock", java.time.Clock.systemUTC());
        IsoMessage msg = builder.build0200("1234567890123456", 999L, "TERM01", "000000");
        byte[] data = msg.writeData();
        IsoParser parser = new IsoParser();
        TestInjection.set(parser, "messageFactory", mf);
        IsoMessage parsed = parser.parse(data);
        assertThat((String) parsed.getObjectValue(2)).isEqualTo("1234567890123456");
    }
}