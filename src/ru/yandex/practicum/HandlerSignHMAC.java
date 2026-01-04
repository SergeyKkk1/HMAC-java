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

public class HandlerSignHMAC extends MyAbstractHttpHandler {

    private final ServiceHMAC service;
    private final PrintWriter log;

    public HandlerSignHMAC(ServiceHMAC service, ConfigStorage.ConfigHMAC config, PrintWriter log) {
        super("POST", config);
        this.service = service;
        this.log = log;
    }

    @Override
    public void handlePost(HttpExchange exchange) throws IOException {
        try {
            InputStream requestBody = exchange.getRequestBody();
            OutputStream responseBody = exchange.getResponseBody();
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            InputStreamReader streamReader = new InputStreamReader(requestBody, StandardCharsets.UTF_8);
            SignRequest request = gson.fromJson(streamReader, SignRequest.class);
            checkContentTypeHeader(exchange);
            byte[] requestMsgBytes = request.getMsg().getBytes(StandardCharsets.UTF_8);
            checkRequestMsg(requestMsgBytes);
            byte[] sign = service.sign(requestMsgBytes);
            SignResponse response = new SignResponse();
            response.setSignature(HelperBase64.encode(sign));
            log.println(String.format("signed msg length %s, result signature length %s", request.getMsg().length(), response.getSignature().length()));
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
        } catch (Exception e) {
            String internalErrorMsg = "internal";
            ErrorMessageDto errorMsg = new ErrorMessageDto(internalErrorMsg);
            sendText(exchange, GSON.toJson(errorMsg), 500);
            e.printStackTrace(new PrintStream(exchange.getResponseBody()));
        } finally {
            exchange.getResponseBody().close();
        }
    }

}
