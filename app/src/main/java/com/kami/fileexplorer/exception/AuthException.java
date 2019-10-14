package com.kami.fileexplorer.exception;

import java.io.IOException;


public class AuthException extends IOException {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
