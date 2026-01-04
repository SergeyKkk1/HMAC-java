package ru.yandex.practicum;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.config.ConfigStorage;
import ru.yandex.practicum.config.HelperBase64;
import ru.yandex.practicum.service.ServiceHMAC;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class HandlerVerifyHMAC extends MyAbstractHttpHandler {

    private final ServiceHMAC service;
    private final PrintWriter log;

    public HandlerVerifyHMAC(ServiceHMAC service, ConfigStorage.ConfigHMAC config, PrintWriter log) {
        super("POST", config);
        this.service = service;
        this.log = log;
    }

    @Override
    public void handlePost(HttpExchange exchange) throws IOException {
        try {
            InputStream requestBody = exchange.getRequestBody();
            OutputStream responseBody = exchange.getResponseBody();
            checkContentTypeHeader(exchange);
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            VerifyRequest request = gson.fromJson(new InputStreamReader(requestBody, StandardCharsets.UTF_8), VerifyRequest.class);
            checkRequestMsg(request.getMsg().getBytes());
            checkRequestSignature(request.getSignature());
            boolean ok = service.verify(request.getMsg().getBytes(StandardCharsets.UTF_8), HelperBase64.decode(request.getSignature()));
            VerifyResponse response = new VerifyResponse();
            response.setOk(ok);
            log.println(String.format("verified msg length %s sign length %s, result %b", request.getMsg().length(),
                    request.getSignature().length(), ok));
            exchange.sendResponseHeaders(200, 0);
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(responseBody, StandardCharsets.UTF_8))) {
                gson.toJson(response, writer);
            }
        } catch (HttpUnsupportedMediaTypeException e) {
            ErrorMessageDto errorMsg = new ErrorMessageDto(e.getError());
            sendText(exchange, GSON.toJson(errorMsg), 415);
            e.printStackTrace(new PrintStream(exchange.getResponseBody()));
        } catch (EmptyMessageException e) {
            ErrorMessageDto errorMsg = new ErrorMessageDto(e.getError());
            sendText(exchange, GSON.toJson(errorMsg), 400);
            e.printStackTrace(new PrintStream(exchange.getResponseBody()));
        } catch (MessageLengthExceededException e) {
            ErrorMessageDto errorMsg = new ErrorMessageDto(e.getError());
            sendText(exchange, GSON.toJson(errorMsg), 413);
            e.printStackTrace(new PrintStream(exchange.getResponseBody()));
        } catch (IncorrectMessageFormatException e) {
            ErrorMessageDto errorMsg = new ErrorMessageDto(e.getError());
            sendText(exchange, GSON.toJson(errorMsg), 400);
            e.printStackTrace(new PrintStream(exchange.getResponseBody()));
        } catch (Exception e) {
            exchange.sendResponseHeaders(500, 0);
            e.printStackTrace(new PrintStream(exchange.getResponseBody()));
        } finally {
            exchange.getResponseBody().close();
        }
    }

    private void checkRequestSignature(String signature) {
        if (signature == null || signature.isEmpty()) {
            throw new IncorrectMessageFormatException("Signature can't be empty", "invalid_signature_format");
        }
        try {
            HelperBase64.decode(signature);
        } catch (IllegalArgumentException e) {
            throw new IncorrectMessageFormatException("Signature encoding is incorrect", "invalid_signature_format");
        }
    }

}
