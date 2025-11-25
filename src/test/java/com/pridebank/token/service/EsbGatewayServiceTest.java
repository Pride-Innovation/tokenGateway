package com.pridebank.token.service;

import com.pridebank.token.TestInjection;
import com.pridebank.token.client.ESBClient;
import com.pridebank.token.dto.AtmTransactionRequest;
import com.pridebank.token.dto.AtmTransactionResponse;
import com.solab.iso8583.IsoMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class EsbGatewayServiceTest {

    private EsbGatewayService svc;
    private ESBClient mock;

    @BeforeEach
    void setup() {
        svc = new EsbGatewayService();
        mock = Mockito.mock(ESBClient.class);
        TestInjection.set(svc, "esbClient", mock);
        TestInjection.set(svc, "atmUsername", "u");
        TestInjection.set(svc, "atmPassword", "p");
    }

    @Test
    void successResponse() {
        AtmTransactionResponse body = AtmTransactionResponse.builder()
                .responseCode("SUCCESS").authorizationCode("AUTH01").build();

        ResponseEntity<AtmTransactionResponse> ok = ResponseEntity.ok(body);
        Mockito.doReturn(ok)
                .when(mock)
                .CardChargePostRequest(anyString(), any(AtmTransactionRequest.class));

        String out = svc.sendToEsb("{\"messageType\":\"0200\",\"cardNumber\":\"123\"}", new IsoMessage());
        assertThat(out).contains("\"responseCode\":\"SUCCESS\"");
        assertThat(out).contains("\"authorizationCode\":\"AUTH01\"");
    }

    @Test
    void failureResponse() {
        ResponseEntity<String> err = ResponseEntity.status(500).body("err");
        Mockito.doReturn(err)
                .when(mock)
                .CardChargePostRequest(anyString(), any(AtmTransactionRequest.class));

        String out = svc.sendToEsb("{\"messageType\":\"0200\"}", new IsoMessage());
        assertThat(out).contains("\"responseCode\":\"SYSTEM_ERROR\"");
    }
}