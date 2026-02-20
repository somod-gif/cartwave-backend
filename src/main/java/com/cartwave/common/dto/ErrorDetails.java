package com.cartwave.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {

    private String errorCode;
    private String errorMessage;
    private Integer statusCode;
    private Instant timestamp;
    private String path;
    private Map<String, String> fieldErrors;

}
