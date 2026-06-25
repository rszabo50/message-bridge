package io.github.rszabo50.messagebridge.webhook;

import io.github.rszabo50.messagebridge.SendResult;

import java.io.IOException;
import java.net.URI;

@FunctionalInterface
public interface WebhookClient {
    SendResult postJson(URI webhookUri, String jsonPayload) throws IOException, InterruptedException;
}
