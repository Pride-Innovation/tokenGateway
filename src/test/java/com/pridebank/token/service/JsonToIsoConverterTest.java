package com.pridebank.token.service;

import com.pridebank.token.TestInjection;
import com.pridebank.token.config.IsoConfig;
import com.pridebank.token.util.ResponseCodeMapper;
import com.pridebank.token.util.StanGenerator;
import com.solab.iso8583.IsoMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonToIsoConverterTest {

    private JsonToIsoConverter converter;
    private IsoMessageBuilder builder;

    @BeforeEach
    void setup() {
        converter = new JsonToIsoConverter();
        builder = new IsoMessageBuilder();
        var mf = new IsoConfig().messageFactory();
        TestInjection.set(builder, "messageFactory", mf);
        TestInjection.set(builder, "stanGenerator", new StanGenerator());
        TestInjection.set(builder, "clock", java.time.Clock.systemUTC());
        TestInjection.set(converter, "isoMessageBuilder", builder);
        ResponseCodeMapper mapper = new ResponseCodeMapper();
        mapper.setCodes(java.util.Map.of("SUCCESS", "00", "SYSTEM_ERROR", "96"));
        TestInjection.set(converter, "responseCodeMapper", mapper);
    }

    @Test
    void buildsResponse() throws Exception {
        IsoMessage req = builder.build0200("1234567890123456", 500L, "TERM01", "000000");
        String json = "{\"responseCode\":\"SUCCESS\",\"authorizationCode\":\"ABC123\",\"availableBalance\":\"250.75\",\"message\":\"OK\"}";
        IsoMessage resp = converter.convert(json, req);
//        assertThat(String.format("%04d", resp.getType())).isEqualTo("0528");
        assertThat((String) resp.getObjectValue(38)).isEqualTo("ABC123");
        assertThat((String) resp.getObjectValue(39)).isEqualTo("00");
//        assertThat((String) resp.getObjectValue(54)).isEqualTo("00000025075");
        assertThat((String) resp.getObjectValue(44)).isEqualTo("OK");
    }
}