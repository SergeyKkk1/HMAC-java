package ru.yandex.practicum;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BaseServerTest {
    protected static final Gson GSON = new Gson();


    protected static final HttpClient CLIENT = HttpClient.newHttpClient();

    protected HttpResponse<String> post(String path, String body) {
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://localhost:8080%s", path)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            return CLIENT.send(postRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
