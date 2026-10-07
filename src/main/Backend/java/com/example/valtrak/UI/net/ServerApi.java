package com.example.valtrak.UI.net;

import com.example.valtrak.Data.GameData.DataTransfer.CardData.CardDto;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * The desktop client's only connection to the game server. Calls here block,
 * so run them off the JavaFX thread.
 * The server address comes from -Dvaltrak.server=... (default http://localhost:8080).
 */
public class ServerApi {

    public static final String BASE_URL =
            System.getProperty("valtrak.server", "http://localhost:8080").replaceAll("/+$", "");

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final JsonMapper JSON = JsonMapper.builder().build();

    public static List<CardDto> fetchCards() throws IOException {
        return get("/cards", new TypeReference<List<CardDto>>() {});
    }

    private static <T> T get(String path, TypeReference<T> type) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("Server returned HTTP " + response.statusCode() + " for " + path);
            }
            return JSON.readValue(response.body(), type);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }
}
