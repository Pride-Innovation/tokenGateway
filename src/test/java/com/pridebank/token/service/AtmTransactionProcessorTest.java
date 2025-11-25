package com.pridebank.token.service;

import com.pridebank.token.TestInjection;
import com.pridebank.token.config.IsoConfig;
import com.pridebank.token.util.StanGenerator;
import com.pridebank.token.validation.IsoValidator;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class AtmTransactionProcessorTest {

    private AtmTransactionProcessor proc;
    private IsoToJsonConverter toJson;
    private JsonToIsoConverter toIso;
    private EsbGatewayService esb;
    private IsoMessageBuilder builder;

    @BeforeEach
    void setup() {
        proc = new AtmTransactionProcessor();
        toJson = Mockito.mock(IsoToJsonConverter.class);
        toIso = Mockito.mock(JsonToIsoConverter.class);
        esb = Mockito.mock(EsbGatewayService.class);
        builder = new IsoMessageBuilder();
        var mf = new IsoConfig().messageFactory();
        TestInjection.set(builder, "messageFactory", mf);
        TestInjection.set(builder, "stanGenerator", new StanGenerator());
        TestInjection.set(builder, "clock", java.time.Clock.systemUTC());
        TestInjection.set(proc, "isoToJsonConverter", toJson);
        TestInjection.set(proc, "jsonToIsoConverter", toIso);
        TestInjection.set(proc, "esbGatewayService", esb);
        TestInjection.set(proc, "messageFactory", mf);
        TestInjection.set(proc, "isoMessageBuilder", builder);
        TestInjection.set(proc, "isoValidator", new IsoValidator());
    }

    @Test
    void successFlow() throws Exception {
        IsoMessage req = builder.build0200("1234567890123456", 100L, "TERM01", "000000");
        Mockito.when(toJson.convert(req)).thenReturn("{}");
        Mockito.when(esb.sendToEsb("{}", req)).thenReturn("{\"responseCode\":\"SUCCESS\"}");
        IsoMessage respMsg = builder.createResponseFromRequest(req, 0x210);
        respMsg.setValue(39, "00", IsoType.ALPHA, 2);
        Mockito.when(toIso.convert("{\"responseCode\":\"SUCCESS\"}", req)).thenReturn(respMsg);
        IsoMessage out = proc.processTransaction(req);
        assertThat((String) out.getObjectValue(39)).isEqualTo("00");
    }

    @Test
    void validationFailureReturnsCode30() throws Exception {
        IsoMessage bad = new IsoMessage();
        bad.setType(0x200); // missing fields
        IsoMessage out = proc.processTransaction(bad);
        assertThat((String) out.getObjectValue(39)).isEqualTo("30");
    }

    @Test
    void exceptionReturns96() throws Exception {
        IsoMessage req = builder.build0200("1234567890123456", 100L, "TERM01", "000000");
        Mockito.when(toJson.convert(req)).thenThrow(new RuntimeException("boom"));
        IsoMessage out = proc.processTransaction(req);
        assertThat((String) out.getObjectValue(39)).isEqualTo("96");
    }
}