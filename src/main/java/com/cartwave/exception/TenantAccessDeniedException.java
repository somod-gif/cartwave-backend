package com.cartwave.exception;

public class TenantAccessDeniedException extends RuntimeException {

    private final String errorCode = "TENANT_ACCESS_DENIED";

    public TenantAccessDeniedException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return errorCode;
    }

}
