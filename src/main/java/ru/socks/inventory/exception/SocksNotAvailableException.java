package ru.socks.inventory.exception;

public class SocksNotAvailableException extends RuntimeException {
    public SocksNotAvailableException() {
        super();
    }

    public SocksNotAvailableException(String message) {
        super(message);
    }
}
