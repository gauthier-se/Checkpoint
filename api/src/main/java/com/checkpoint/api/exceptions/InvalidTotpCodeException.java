package com.checkpoint.api.exceptions;

public class InvalidTotpCodeException extends RuntimeException {

    public InvalidTotpCodeException(String message) {
        super(message);
    }
}
