package ru.yandex.practicum;

public class IncorrectMessageFormatException extends RuntimeException {
    private final String error;

    public IncorrectMessageFormatException(String message, String error) {
        super(message);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
