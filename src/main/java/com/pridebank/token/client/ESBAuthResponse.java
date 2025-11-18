package com.pridebank.token.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ESBAuthResponse {

    @JsonProperty("authenticated")
    private boolean authenticated;

    @JsonProperty("message")
    private String message;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("username")
    private String username;
}