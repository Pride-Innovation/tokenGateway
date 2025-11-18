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
public class ChargeRequest {
    @NotBlank(message = "Token is required")
    @JsonProperty("userauthtoken")
    private String authToken;

    @NotBlank(message = "Account Number is required")
    private String debit_account;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotBlank(message = "Narration is required")
    private String narration;

    @NotBlank(message = "Transaction ID is required")
    private String transaction_id;

    @NotBlank(message = "Branch ID is required")
    private String branch;

}
