package ru.yandex.practicum;

class VerifyResponse {
    private boolean ok;

    public VerifyResponse(boolean ok) {
        this.ok = ok;
    }

    public VerifyResponse() {
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }
}
