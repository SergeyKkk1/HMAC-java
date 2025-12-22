package ru.yandex.practicum;

public class EmptyMessageException extends RuntimeException {
    private final String error;

    public EmptyMessageException(String message, String error) {
        super(message);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
