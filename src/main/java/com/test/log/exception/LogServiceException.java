package com.test.log.exception;

public class LogServiceException extends RuntimeException {
    public LogServiceException(String msg) {
        super(msg);
    }
}
