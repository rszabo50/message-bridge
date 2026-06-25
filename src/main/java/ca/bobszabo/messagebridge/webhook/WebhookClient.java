package ca.bobszabo.messagebridge.webhook;

import ca.bobszabo.messagebridge.SendResult;

import java.io.IOException;
import java.net.URI;

@FunctionalInterface
public interface WebhookClient {
    SendResult postJson(URI webhookUri, String jsonPayload) throws IOException, InterruptedException;
}
