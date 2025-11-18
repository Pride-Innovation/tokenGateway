package com.pridebank.token.client;

import com.pridebank.token.dto.AccountValidationResponse;
import com.pridebank.token.dto.ChargeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@FeignClient(
        name = "esb-client",
        url = "${esb.base-url}",
        configuration = ESBFeignConfig.class
)
public interface ESBClient {

    /**
     * Authenticate with ESB using Basic Auth
     * POST /Token/Authentication
     */
    @PostMapping(
            value = "${esb.auth-endpoint}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<ESBAuthResponse> authenticate(
            @RequestHeader("Authorization") String authorizationHeader
    );

    /**
     * Validate account number with ESB
     * GET /AccountNumber/validation?accountnumber=xxx
     */
    @GetMapping(
            value = "${esb.account-validation}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<AccountValidationResponse> validateAccount(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("accountnumber") String accountNumber
    );

    /**
     * Forward card charge POST request to ESB
     */
    @PostMapping(
            value = "${esb.account-charge}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<ChargeResponse> CardChargePostRequest(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Object requestBody
    );
}