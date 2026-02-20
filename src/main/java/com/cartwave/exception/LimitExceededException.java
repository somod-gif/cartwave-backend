package com.cartwave.exception;

public class LimitExceededException extends BusinessException {

    public LimitExceededException(String message) {
        super("LIMIT_EXCEEDED", message);
    }

}

