package com.pridebank.token.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountValidationResponse {

    private String status;
    private String customerId;
    private String tittle;
    private String name;
    private String surname;
    private String displayName;
    private String address1;
    private String address2;
    private String email;
    private String mobile;
    private String branch;
    private String account1;
    private String accountType1;
    private String accountCurrency1;
    private String account2;
    private String accountType2;
    private String accountCurrency2;
    private String cashLimit;
    private String goodsLimit;
    private String response; // For error responses

}