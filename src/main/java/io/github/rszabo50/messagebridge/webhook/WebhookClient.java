package io.github.rszabo50.messagebridge.webhook;

import io.github.rszabo50.messagebridge.SendResult;

import java.io.IOException;
import java.net.URI;

/**
 * HTTP transport abstraction for posting webhook JSON payloads.
 */
@FunctionalInterface
public interface WebhookClient {
    /**
     * Posts a JSON payload to a webhook URI.
     *
     * @param webhookUri destination webhook URI
     * @param jsonPayload JSON payload body
     * @return send result returned by the destination
     * @throws IOException when transport fails
     * @throws InterruptedException when the sending thread is interrupted
     */
    SendResult postJson(URI webhookUri, String jsonPayload) throws IOException, InterruptedException;
}
