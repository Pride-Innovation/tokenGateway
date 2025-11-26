package com.pridebank.token.service;

import com.solab.iso8583.IsoMessage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IsoParserTest {

    @Test
    void parseRoundTrip() throws Exception {

        var mf = new com.pridebank.token.config.IsoConfig().messageFactory();
        IsoMessageBuilder builder = new IsoMessageBuilder();
        com.pridebank.token.TestInjection.set(builder, "messageFactory", mf);
        com.pridebank.token.TestInjection.set(builder, "stanGenerator", new com.pridebank.token.util.StanGenerator());
        com.pridebank.token.TestInjection.set(builder, "clock", java.time.Clock.systemUTC());

        IsoMessage msg = builder.build0200("1234567890123456", 999L, "TERM01", "000000");
        byte[] data = msg.writeData();

        IsoParser parser = new IsoParser();
        com.pridebank.token.TestInjection.set(parser, "messageFactory", mf); // same instance
        IsoMessage parsed = parser.parse(data);
        assertThat(parsed.getType()).isEqualTo(0x200);
        assertThat(parsed.hasField(2)).isTrue();
    }
}