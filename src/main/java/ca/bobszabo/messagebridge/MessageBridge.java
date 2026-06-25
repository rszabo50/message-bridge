package ca.bobszabo.messagebridge;

import ca.bobszabo.messagebridge.webhook.ChatPlatform;
import ca.bobszabo.messagebridge.webhook.JavaHttpWebhookClient;
import ca.bobszabo.messagebridge.webhook.WebhookClient;
import ca.bobszabo.messagebridge.webhook.WebhookMessageSender;

import java.net.URI;
import java.util.Objects;

/**
 * Static entry point for constructing message bridge senders.
 */
public final class MessageBridge {
    private MessageBridge() {
    }

    /**
     * Returns the library artifact name.
     *
     * @return the artifact name
     */
    public static String name() {
        return "message-bridge";
    }

    /**
     * Creates a webhook sender backed by Java's built-in HTTP client.
     *
     * @param platform the target chat platform
     * @param webhookUri the platform webhook URI
     * @return a sender that posts messages to the webhook URI
     */
    public static MessageSender webhookSender(ChatPlatform platform, URI webhookUri) {
        return webhookSender(platform, webhookUri, new JavaHttpWebhookClient());
    }

    /**
     * Creates a webhook sender with a caller-provided webhook client.
     *
     * <p>This overload is intended for tests and for applications that need custom
     * HTTP transport behavior.</p>
     *
     * @param platform the target chat platform
     * @param webhookUri the platform webhook URI
     * @param webhookClient the client used to post JSON payloads
     * @return a sender that posts messages to the webhook URI
     */
    public static MessageSender webhookSender(ChatPlatform platform, URI webhookUri, WebhookClient webhookClient) {
        return new WebhookMessageSender(
                Objects.requireNonNull(platform, "platform"),
                Objects.requireNonNull(webhookUri, "webhookUri"),
                Objects.requireNonNull(webhookClient, "webhookClient"));
    }
}
