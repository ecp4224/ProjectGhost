package com.boxtrotstudio.ghost.utils;

public class MethodDeprecatedException extends RuntimeException {

    public MethodDeprecatedException() {
    }

    public MethodDeprecatedException(String message) {
        super(message);
    }

    public MethodDeprecatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
