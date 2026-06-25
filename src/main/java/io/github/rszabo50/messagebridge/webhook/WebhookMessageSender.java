package io.github.rszabo50.messagebridge.webhook;

import io.github.rszabo50.messagebridge.MessageSender;
import io.github.rszabo50.messagebridge.OutboundMessage;
import io.github.rszabo50.messagebridge.SendResult;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

public final class WebhookMessageSender implements MessageSender {
    private final ChatPlatform platform;
    private final URI webhookUri;
    private final WebhookClient webhookClient;

    public WebhookMessageSender(ChatPlatform platform, URI webhookUri, WebhookClient webhookClient) {
        this.platform = Objects.requireNonNull(platform, "platform");
        this.webhookUri = Objects.requireNonNull(webhookUri, "webhookUri");
        this.webhookClient = Objects.requireNonNull(webhookClient, "webhookClient");
    }

    @Override
    public SendResult send(OutboundMessage message) throws IOException, InterruptedException {
        Objects.requireNonNull(message, "message");
        return webhookClient.postJson(webhookUri, WebhookPayloads.toJson(platform, message));
    }
}
