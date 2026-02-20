package com.cartwave.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    @Builder.Default
    private String refreshToken = "";
    @Builder.Default
    @JsonProperty("token_type")
    private String tokenType = "Bearer";
    private Long expiresIn;

}
