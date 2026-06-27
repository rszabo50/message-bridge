package io.github.rszabo50.messagebridge;

import io.github.rszabo50.messagebridge.webhook.ChatPlatform;
import io.github.rszabo50.messagebridge.webhook.discord.DiscordMessage;
import io.github.rszabo50.messagebridge.webhook.slack.SlackMessage;
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
    void sendsSlackRichWebhookMessage() throws Exception {
        String label = testLabel();
        sendIfConfigured(
                ChatPlatform.SLACK,
                "MESSAGE_BRIDGE_SLACK_WEBHOOK_URL",
                SlackMessage.builder("message-bridge rich Slack integration test [" + label + "]")
                        .markdownSection("*message-bridge rich Slack integration test*")
                        .markdownFields("*Label*\n" + label, "*Platform*\nSlack")
                        .markdownContext("sent at " + Instant.now())
                        .build());
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
    void sendsDiscordRichWebhookMessage() throws Exception {
        String label = testLabel();
        sendIfConfigured(
                ChatPlatform.DISCORD,
                "MESSAGE_BRIDGE_DISCORD_WEBHOOK_URL",
                DiscordMessage.builder("message-bridge rich Discord integration test [" + label + "]")
                        .embed(embed -> embed
                                .title("message-bridge rich Discord integration test")
                                .description("Discord embed payload sent through message-bridge.")
                                .color(0x5865F2)
                                .field("Label", label, true)
                                .field("Platform", "Discord", true)
                                .timestamp(Instant.now())
                                .footer("message-bridge"))
                        .build());
    }

    @Test
    void sendsTeamsWebhookMessage() throws Exception {
        sendIfConfigured(ChatPlatform.MICROSOFT_TEAMS, "MESSAGE_BRIDGE_TEAMS_WEBHOOK_URL");
    }

    private static void sendIfConfigured(ChatPlatform platform, String environmentVariable) throws Exception {
        sendIfConfigured(platform, environmentVariable, OutboundMessage.text(
                "message-bridge integration test [" + testLabel() + "]: " + platform + " at " + Instant.now()));
    }

    private static void sendIfConfigured(
            ChatPlatform platform,
            String environmentVariable,
            OutboundMessage message) throws Exception {
        String webhookUrl = System.getenv(environmentVariable);
        assumeTrue(webhookUrl != null && !webhookUrl.isBlank(), environmentVariable + " is not configured");

        MessageSender sender = MessageBridge.webhookSender(platform, URI.create(webhookUrl));
        SendResult result = sender.send(message);

        assertTrue(result.success(), () -> platform + " webhook returned " + result.statusCode()
                + " with body: " + result.body());
    }

    private static String testLabel() {
        return System.getenv().getOrDefault("MESSAGE_BRIDGE_TEST_LABEL", "local");
    }
}
