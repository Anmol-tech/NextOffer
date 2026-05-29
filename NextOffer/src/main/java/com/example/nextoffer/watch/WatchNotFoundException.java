package com.example.nextoffer.watch;

public class WatchNotFoundException extends RuntimeException {

    public WatchNotFoundException(Long id) {
        super("Company watch not found: " + id);
    }
}
