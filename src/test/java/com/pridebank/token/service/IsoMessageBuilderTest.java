package com.pridebank.token.service;

import com.pridebank.token.TestInjection;
import com.pridebank.token.config.IsoConfig;
import com.pridebank.token.util.StanGenerator;
import com.solab.iso8583.IsoMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class IsoMessageBuilderTest {

    private IsoMessageBuilder builder;

    @BeforeEach
    void setup() {
        builder = new IsoMessageBuilder();
        var mf = new IsoConfig().messageFactory();
        TestInjection.set(builder, "messageFactory", mf);
        TestInjection.set(builder, "stanGenerator", new StanGenerator());
        TestInjection.set(builder, "clock",
                Clock.fixed(Instant.parse("2025-11-25T10:15:30Z"), ZoneId.systemDefault()));
    }

    @Test
    void build0200_populatesMandatoryFields() {
        IsoMessage m = builder.build0200("1234567890123456", 1500L, "TERM01", "000000");

        assertThat(m.getType()).isEqualTo(0x200);
        assertThat(m.getObjectValue(4).toString()).isEqualTo("000000001500");
        assertThat(m.getObjectValue(41).toString()).isEqualTo("TERM01  ");
        assertThat(m.hasField(11)).isTrue();
        assertThat(m.getObjectValue(49).toString()).isEqualTo("566");
    }
}