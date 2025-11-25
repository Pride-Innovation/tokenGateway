package com.pridebank.token.service;

import com.pridebank.token.TestInjection;
import com.pridebank.token.config.IsoConfig;
import com.pridebank.token.util.StanGenerator;
import com.solab.iso8583.IsoMessage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessingCodeVariantsTest {

    @ParameterizedTest
    @CsvSource({
            "000000,Withdrawal",
            "300000,BalanceInquiry",
            "200000,Deposit"
    })
    void buildDifferentProcessingCodes(String code, String label) {
        IsoMessageBuilder builder = new IsoMessageBuilder();
        var mf = new IsoConfig().messageFactory();
        TestInjection.set(builder, "messageFactory", mf);
        TestInjection.set(builder, "stanGenerator", new StanGenerator());
        TestInjection.set(builder, "clock", java.time.Clock.systemUTC());

        IsoMessage msg = builder.build0200("1234567890123456", 1000L, "TERM01", code);
        assertThat((String) msg.getObjectValue(3)).isEqualTo(code);
        assertThat(msg.hasField(4)).isTrue();
    }
}