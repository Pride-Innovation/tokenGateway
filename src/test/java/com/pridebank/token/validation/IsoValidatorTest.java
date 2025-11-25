package com.pridebank.token.validation;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IsoValidatorTest {

    @Test
    void valid0200() {
        IsoMessage m = new IsoMessage();
        m.setType(0x200);
        m.setValue(2, "1234567890123", IsoType.LLVAR, 13);
        m.setValue(3, "000000", IsoType.NUMERIC, 6);
        m.setValue(4, "000000001000", IsoType.NUMERIC, 12);
        m.setValue(7, "1125101530", IsoType.NUMERIC, 10);
        m.setValue(11, "123456", IsoType.NUMERIC, 6);
        m.setValue(41, "TERM01  ", IsoType.ALPHA, 8);
        m.setValue(49, "566", IsoType.NUMERIC, 3);
        IsoValidator v = new IsoValidator();
        var r = v.validate0200(m);
        assertThat(r.isValid()).isTrue();
    }

    @Test
    void missingFields() {
        IsoMessage m = new IsoMessage();
        m.setType(0x200);
        IsoValidator v = new IsoValidator();
        var r = v.validate0200(m);
        assertThat(r.isValid()).isFalse();
        assertThat(r.getErrors()).contains("Missing field 2", "Missing field 3");
    }
}