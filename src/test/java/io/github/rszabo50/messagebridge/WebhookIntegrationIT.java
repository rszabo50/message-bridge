package io.github.rszabo50.messagebridge;

import io.github.rszabo50.messagebridge.webhook.ChatPlatform;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class WebhookIntegrationIT {
    @Test
    void sendsSlackWebhookMessage() throws Exception {
        sendIfConfigured(ChatPlatform.SLACK, "MESSAGE_BRIDGE_SLACK_WEBHOOK_URL");
    }

    @Test
    void sendsMattermostWebhookMessage() throws Exception {
        sendIfConfigured(ChatPlatform.MATTERMOST, "MESSAGE_BRIDGE_MATTERMOST_WEBHOOK_URL");
    }

    @Test
    void sendsDiscordWebhookMessage() throws Exception {
        sendIfConfigured(ChatPlatform.DISCORD, "MESSAGE_BRIDGE_DISCORD_WEBHOOK_URL");
    }

    @Test
    void sendsTeamsWebhookMessage() throws Exception {
        sendIfConfigured(ChatPlatform.MICROSOFT_TEAMS, "MESSAGE_BRIDGE_TEAMS_WEBHOOK_URL");
    }

    private static void sendIfConfigured(ChatPlatform platform, String environmentVariable) throws Exception {
        String webhookUrl = System.getenv(environmentVariable);
        assumeTrue(webhookUrl != null && !webhookUrl.isBlank(), environmentVariable + " is not configured");

        MessageSender sender = MessageBridge.webhookSender(platform, URI.create(webhookUrl));
        SendResult result = sender.send(OutboundMessage.text(
                "message-bridge integration test: " + platform + " at " + Instant.now()));

        assertTrue(result.success(), () -> platform + " webhook returned " + result.statusCode()
                + " with body: " + result.body());
    }
}
