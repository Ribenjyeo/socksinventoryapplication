package ru.socks.inventory.exception;

public class ConflictOutcomeSocksException extends RuntimeException {
    public ConflictOutcomeSocksException() {
        super();
    }

    public ConflictOutcomeSocksException(String message) {
        super(message);
    }
}
