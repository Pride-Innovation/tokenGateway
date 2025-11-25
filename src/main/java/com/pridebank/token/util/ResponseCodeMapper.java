package com.pridebank.token.util;

import java.util.HashMap;
import java.util.Map;

public class ResponseCodeMapper {

    private static final Map<String, String> ESB_TO_ISO_MAP = new HashMap<>();

    static {
        ESB_TO_ISO_MAP.put("SUCCESS", "00");
        ESB_TO_ISO_MAP.put("INSUFFICIENT_FUNDS", "51");
        ESB_TO_ISO_MAP.put("INVALID_ACCOUNT", "14");
        ESB_TO_ISO_MAP.put("INVALID_PIN", "55");
        ESB_TO_ISO_MAP.put("LIMIT_EXCEEDED", "61");
        ESB_TO_ISO_MAP.put("TIMEOUT", "68");
        ESB_TO_ISO_MAP.put("SYSTEM_ERROR", "96");
    }

    public static String mapEsbToIso(String esbCode) {
        return ESB_TO_ISO_MAP.getOrDefault(esbCode, "96");
    }
}