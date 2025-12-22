package ru.yandex.practicum;

public class MessageLengthExceededException extends RuntimeException {
    private final String error;

    public MessageLengthExceededException(String message, String error) {
        super(message);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
