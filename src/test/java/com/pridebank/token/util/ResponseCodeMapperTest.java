package com.pridebank.token.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseCodeMapperTest {

    @Test
    void mapsCodes() {
        ResponseCodeMapper mapper = new ResponseCodeMapper();
        mapper.setCodes(java.util.Map.of(
                "SUCCESS", "00", "SYSTEM_ERROR", "96", "INVALID_PIN", "55"
        ));
        assertThat(mapper.mapEsbToIso("SUCCESS")).isEqualTo("00");
        assertThat(mapper.mapEsbToIso("INVALID_PIN")).isEqualTo("55");
    }

    @Test
    void defaultsToSystemError() {
        ResponseCodeMapper mapper = new ResponseCodeMapper();
        mapper.setCodes(java.util.Map.of("SYSTEM_ERROR", "96"));
        assertThat(mapper.mapEsbToIso("X")).isEqualTo("96");
    }
}