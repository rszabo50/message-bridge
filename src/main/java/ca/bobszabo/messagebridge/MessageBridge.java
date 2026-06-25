package ca.bobszabo.messagebridge;

import ca.bobszabo.messagebridge.webhook.ChatPlatform;
import ca.bobszabo.messagebridge.webhook.JavaHttpWebhookClient;
import ca.bobszabo.messagebridge.webhook.WebhookClient;
import ca.bobszabo.messagebridge.webhook.WebhookMessageSender;

import java.net.URI;
import java.util.Objects;

public final class MessageBridge {
    private MessageBridge() {
    }

    public static String name() {
        return "message-bridge";
    }

    public static MessageSender webhookSender(ChatPlatform platform, URI webhookUri) {
        return webhookSender(platform, webhookUri, new JavaHttpWebhookClient());
    }

    public static MessageSender webhookSender(ChatPlatform platform, URI webhookUri, WebhookClient webhookClient) {
        return new WebhookMessageSender(
                Objects.requireNonNull(platform, "platform"),
                Objects.requireNonNull(webhookUri, "webhookUri"),
                Objects.requireNonNull(webhookClient, "webhookClient"));
    }
}
