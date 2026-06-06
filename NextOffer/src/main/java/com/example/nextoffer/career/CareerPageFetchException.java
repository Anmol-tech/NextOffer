package com.example.nextoffer.career;

public class CareerPageFetchException extends RuntimeException {

    public CareerPageFetchException(String message) {
        super(message);
    }

    public CareerPageFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
