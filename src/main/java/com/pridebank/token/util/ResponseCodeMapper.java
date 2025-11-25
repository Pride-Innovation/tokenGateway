package com.pridebank.token.util;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Setter
@Component
@ConfigurationProperties(prefix = "esb")
public class ResponseCodeMapper {

    private Map<String, String> codes = new HashMap<>();

    public String mapEsbToIso(String esbCode) {
        if (esbCode == null || esbCode.isBlank()) return codes.getOrDefault("SYSTEM_ERROR", "96");
        return codes.getOrDefault(esbCode, codes.getOrDefault("SYSTEM_ERROR", "96"));
    }
}