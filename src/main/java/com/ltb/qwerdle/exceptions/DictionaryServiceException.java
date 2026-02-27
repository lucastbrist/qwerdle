package com.ltb.qwerdle.exceptions;

public class DictionaryServiceException extends RuntimeException {

    public DictionaryServiceException(String message) {
        super(message);
    }

    public DictionaryServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}