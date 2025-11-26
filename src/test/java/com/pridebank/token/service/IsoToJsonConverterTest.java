package com.pridebank.token.service;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IsoToJsonConverterTest {

    @Test
    void convertsFields() throws Exception {
        IsoMessage m = new IsoMessage();
        m.setType(0x200);
        m.setValue(2, "1234567890123456", IsoType.LLVAR, 16);
        m.setValue(3, "000000", IsoType.NUMERIC, 6);
        m.setValue(4, "000000010000", IsoType.NUMERIC, 12);
        m.setValue(11, "123456", IsoType.NUMERIC, 6);
        m.setValue(41, "TERM01  ", IsoType.ALPHA, 8);
        m.setValue(49, "566", IsoType.NUMERIC, 3);

        IsoToJsonConverter c = new IsoToJsonConverter();
        String json = c.convert(m);
        assertThat(json).contains("\"messageType\":\"0512\"");
        assertThat(json).contains("\"amount\":\"100.00\"");
        assertThat(json).contains("\"cardNumber\":\"123456******3456\"");
    }
}