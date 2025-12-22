package ru.yandex.practicum;

import java.util.Objects;

final class ErrorMessageDto {
    private final String error;

    ErrorMessageDto(String error) {
        this.error = error;
    }

    public String error() {
        return error;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ErrorMessageDto) obj;
        return Objects.equals(this.error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(error);
    }

    @Override
    public String toString() {
        return "ErrorMessageDto[" +
                "error=" + error + ']';
    }

}
