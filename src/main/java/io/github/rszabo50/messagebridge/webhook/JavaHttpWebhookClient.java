package io.github.rszabo50.messagebridge.webhook;

import io.github.rszabo50.messagebridge.SendResult;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

public final class JavaHttpWebhookClient implements WebhookClient {
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final Duration requestTimeout;

    public JavaHttpWebhookClient() {
        this(HttpClient.newHttpClient(), DEFAULT_REQUEST_TIMEOUT);
    }

    public JavaHttpWebhookClient(HttpClient httpClient, Duration requestTimeout) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
    }

    @Override
    public SendResult postJson(URI webhookUri, String jsonPayload) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(Objects.requireNonNull(webhookUri, "webhookUri"))
                .timeout(requestTimeout)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(Objects.requireNonNull(jsonPayload, "jsonPayload")))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return new SendResult(response.statusCode(), response.body());
    }
}
