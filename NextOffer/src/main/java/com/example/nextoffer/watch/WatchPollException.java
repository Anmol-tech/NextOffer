package com.example.nextoffer.watch;

public class WatchPollException extends RuntimeException {

    public WatchPollException(Long watchId, Throwable cause) {
        super("Failed to poll company watch " + watchId + ": " + cause.getMessage(), cause);
    }
}
