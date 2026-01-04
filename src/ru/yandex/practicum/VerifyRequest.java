package ru.yandex.practicum;

class VerifyRequest {
    String msg;
    String signature;

    public VerifyRequest(String msg, String signature) {
        this.msg = msg;
        this.signature = signature;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
