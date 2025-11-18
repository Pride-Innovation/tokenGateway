package com.pridebank.token.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyAccountRequest {

    @NotBlank(message = "Token is required")
    @JsonProperty("userauthtoken")
    private String authToken;

    @NotBlank(message = "Account Number is required")
    @JsonProperty("account_number")
    private String accountNumber;
}
