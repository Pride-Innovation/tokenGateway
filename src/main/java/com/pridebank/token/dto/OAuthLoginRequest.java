package com.pridebank.token.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth-style login request with form-urlencoded parameters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthLoginRequest {

    @NotBlank(message = "grant_type is required")
    private String grant_type;

    @NotBlank(message = "client_id is required")
    private String client_id;

    @NotBlank(message = "client_secret is required")
    private String client_secret;
}
