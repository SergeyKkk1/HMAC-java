package ru.yandex.practicum;

public class HttpUnsupportedMediaTypeException extends RuntimeException {
    private final String error;

    public HttpUnsupportedMediaTypeException(String message, String error) {
        super(message);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
