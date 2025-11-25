package com.pridebank.token.service;

import com.solab.iso8583.IsoMessage;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * IsoClient
 * ---------
 * This class is responsible for low-level ISO-8583 network communication.
 * <p>
 * Most ISO-8583 hosts require:
 * - A persistent TCP/IP socket
 * - A 2-byte message length header in big-endian format
 * - Raw ISO-8583 payload following the length header
 * <p>
 * This client:
 * 1. Packs the IsoMessage into a byte array using message.writeData()
 * 2. Sends the message length (2 bytes)
 * 3. Sends the actual ISO-8583 message content
 * 4. Reads the length header of the response
 * 5. Reads the full response message
 * <p>
 * It does NOT parse the message. That is the job of IsoParser.
 */
@Service
public class IsoClient {

    /**
     * Sends a packed ISO-8583 message to an ISO switch or host.
     *
     * @param host    IP address of the ISO host
     * @param port    Port of the ISO host
     * @param message The populated IsoMessage (0200, 0800, etc.)
     * @return Raw ISO-8583 response bytes (0210, 0810, etc.)
     */
    public byte[] send(String host, int port, IsoMessage message) throws Exception {

        // Convert IsoMessage into raw byte[] according to iso8583.xml packager
        byte[] isoData = message.writeData();

        try (Socket socket = new Socket(host, port);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {

            /*
              ===============================
               1. Send 2-byte message length
              ===============================
              Most ISO hosts expect:

                Byte 1 = length high-byte
                Byte 2 = length low-byte

              This is NOT part of ISO-8583 itself, but a framing protocol used
              in almost all financial hosts and switches.
             */
            out.write((isoData.length >> 8) & 0xFF);
            out.write(isoData.length & 0xFF);

            // Send raw ISO-8583 message bytes
            out.write(isoData);
            out.flush();

            /*
              ===============================
               2. Read 2-byte length header
              ===============================
              Tells us how many bytes the host will send back.
             */
            byte[] lenBytes = in.readNBytes(2);
            int respLen = (lenBytes[0] << 8) + lenBytes[1];

            /*
              ===============================
               3. Read the actual response
              ===============================
             */
            return in.readNBytes(respLen);
        }
    }
}
