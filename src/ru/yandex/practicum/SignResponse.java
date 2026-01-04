package ru.yandex.practicum;

public class SignResponse {
    String signature;

    public SignResponse(String signature) {
        this.signature = signature;
    }

    public SignResponse() {
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
