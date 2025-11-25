package com.pridebank.token.server;

import com.pridebank.token.config.IsoConfig;
import com.pridebank.token.service.*;
import com.pridebank.token.util.ResponseCodeMapper;
import com.pridebank.token.util.StanGenerator;
import com.pridebank.token.validation.IsoValidator;
import com.solab.iso8583.IsoMessage;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;

class IsoTcpServerIntegrationTest {

    private IsoTcpServer server;
    private int port;

    @BeforeEach
    void start() throws Exception {
        try (ServerSocket ss = new ServerSocket(0)) {
            port = ss.getLocalPort();
        }
        var mf = new IsoConfig().messageFactory();

        IsoMessageBuilder builder = new IsoMessageBuilder();
        ReflectionTestUtils.setField(builder, "messageFactory", mf);
        ReflectionTestUtils.setField(builder, "stanGenerator", new StanGenerator());
        ReflectionTestUtils.setField(builder, "clock", java.time.Clock.systemUTC());

        // Processor that short-circuits ESB to SUCCESS
        AtmTransactionProcessor proc = new AtmTransactionProcessor();
        ReflectionTestUtils.setField(proc, "isoMessageBuilder", builder);
        ReflectionTestUtils.setField(proc, "messageFactory", mf);
        ReflectionTestUtils.setField(proc, "isoValidator", new IsoValidator());

        IsoToJsonConverter toJson = new IsoToJsonConverter();
        JsonToIsoConverter toIso = new JsonToIsoConverter();
        ResponseCodeMapper mapper = new ResponseCodeMapper();
        mapper.setCodes(java.util.Map.of("SUCCESS", "00", "SYSTEM_ERROR", "96"));
        ReflectionTestUtils.setField(toIso, "isoMessageBuilder", builder);
        ReflectionTestUtils.setField(toIso, "responseCodeMapper", mapper);

        ReflectionTestUtils.setField(proc, "isoToJsonConverter", toJson);
        ReflectionTestUtils.setField(proc, "jsonToIsoConverter", toIso);
        // Fake ESB always returns SUCCESS JSON
        EsbGatewayService esb = new EsbGatewayService();
        ReflectionTestUtils.setField(esb, "atmUsername", "u");
        ReflectionTestUtils.setField(esb, "atmPassword", "p");
        ReflectionTestUtils.setField(proc, "esbGatewayService", esb);
        ReflectionTestUtils.setField(esb, "esbClient", (Object) null); // unused

        server = new IsoTcpServer(mf, proc);
        ReflectionTestUtils.setField(server, "port", port);
        server.start();
    }

    @AfterEach
    void stop() throws Exception {
        server.stop();
    }

    @Test
    void roundTrip() throws Exception {
        var mf = new IsoConfig().messageFactory();
        IsoMessageBuilder builder = new IsoMessageBuilder();
        ReflectionTestUtils.setField(builder, "messageFactory", mf);
        ReflectionTestUtils.setField(builder, "stanGenerator", new StanGenerator());
        ReflectionTestUtils.setField(builder, "clock", java.time.Clock.systemUTC());

        IsoMessage req = builder.build0200("1234567890123456", 500L, "TERM01", "000000");
        byte[] data = req.writeData();

        try (Socket s = new Socket("127.0.0.1", port);
             OutputStream out = s.getOutputStream();
             InputStream in = s.getInputStream()) {

            out.write((data.length >> 8) & 0xFF);
            out.write(data.length & 0xFF);
            out.write(data);
            out.flush();

            byte[] lenBytes = in.readNBytes(2);
            int len = ((lenBytes[0] & 0xFF) << 8) | (lenBytes[1] & 0xFF);
            byte[] resp = in.readNBytes(len);

            IsoMessage parsed = mf.parseMessage(resp, 0);
            assertThat(String.format("%04d", parsed.getType())).isEqualTo("0210");
            assertThat(parsed.hasField(39)).isTrue();
        }
    }
}