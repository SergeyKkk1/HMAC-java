package ru.yandex.practicum;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.config.ConfigStorage;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class MyAbstractHttpHandler implements HttpHandler {

    private final String method;
    protected static final Gson GSON = new Gson();
    protected final ConfigStorage.ConfigHMAC configStorage;

    public MyAbstractHttpHandler(String method, ConfigStorage.ConfigHMAC configStorage) {
        this.method = method;
        this.configStorage = configStorage;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals(method)) {
            System.out.println("unknown method: " + exchange.getRequestMethod());
            exchange.sendResponseHeaders(405, -1);
        }
        switch (exchange.getRequestMethod()) {
            case "POST":
                handlePost(exchange);
                break;
            default:
                throw new RuntimeException("unknown method: " + exchange.getRequestMethod());
        }
    }

    protected abstract void handlePost(HttpExchange exchange) throws IOException;

    protected void sendText(HttpExchange exchange, String responseText, Integer responseCode) throws IOException {
        exchange.sendResponseHeaders(responseCode, responseText.getBytes().length);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        OutputStream os = exchange.getResponseBody();
        os.write(responseText.getBytes());
        os.close();
    }

    protected void checkRequestMsg(byte[] requestMsgBytes) {
        String message = new String(requestMsgBytes, StandardCharsets.UTF_8);
        if (requestMsgBytes.length == 0) {
            throw new EmptyMessageException("Request message is empty", "invalid_json");
        } else if (message.length() > configStorage.getMaxMsgSizeBytes()) {
            throw new MessageLengthExceededException(String.format("Message length %s is more than max allowed",
                    message.length()), "invalid_msg");
        }
    }
}
