package ca.bobszabo.messagebridge;

import ca.bobszabo.messagebridge.webhook.ChatPlatform;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageBridgeTest {
    @Test
    void exposesProjectName() {
        assertEquals("message-bridge", MessageBridge.name());
    }

    @Test
    void createsSlackWebhookSender() throws Exception {
        CapturingWebhookClient webhookClient = new CapturingWebhookClient();
        MessageSender sender = MessageBridge.webhookSender(
                ChatPlatform.SLACK,
                URI.create("https://hooks.slack.test/services/example"),
                webhookClient);

        SendResult result = sender.send(OutboundMessage.text("Build passed")
                .withPlatformOverride("username", "message-bridge"));

        assertTrue(result.success());
        assertEquals("https://hooks.slack.test/services/example", webhookClient.webhookUri.toString());
        assertEquals("{\"text\":\"Build passed\",\"username\":\"message-bridge\"}", webhookClient.jsonPayload);
    }

    @Test
    void createsDiscordWebhookPayload() throws Exception {
        CapturingWebhookClient webhookClient = new CapturingWebhookClient();
        MessageSender sender = MessageBridge.webhookSender(
                ChatPlatform.DISCORD,
                URI.create("https://discord.test/api/webhooks/example"),
                webhookClient);

        sender.send(OutboundMessage.text("Deployment started")
                .withPlatformOverride("allowed_mentions", Map.of("parse", List.of())));

        assertEquals(
                "{\"content\":\"Deployment started\",\"allowed_mentions\":{\"parse\":[]}}",
                webhookClient.jsonPayload);
    }

    @Test
    void createsMattermostWebhookPayload() throws Exception {
        CapturingWebhookClient webhookClient = new CapturingWebhookClient();
        MessageSender sender = MessageBridge.webhookSender(
                ChatPlatform.MATTERMOST,
                URI.create("https://mattermost.test/hooks/example"),
                webhookClient);

        sender.send(OutboundMessage.text("Review requested"));

        assertEquals("{\"text\":\"Review requested\"}", webhookClient.jsonPayload);
    }

    @Test
    void createsTeamsMessageCardPayload() throws Exception {
        CapturingWebhookClient webhookClient = new CapturingWebhookClient();
        MessageSender sender = MessageBridge.webhookSender(
                ChatPlatform.MICROSOFT_TEAMS,
                URI.create("https://teams.test/webhook/example"),
                webhookClient);

        sender.send(OutboundMessage.text("Incident resolved"));

        assertEquals(
                "{\"@type\":\"MessageCard\",\"@context\":\"https://schema.org/extensions\","
                        + "\"summary\":\"Incident resolved\",\"text\":\"Incident resolved\"}",
                webhookClient.jsonPayload);
    }

    private static final class CapturingWebhookClient implements ca.bobszabo.messagebridge.webhook.WebhookClient {
        private URI webhookUri;
        private String jsonPayload;

        @Override
        public SendResult postJson(URI webhookUri, String jsonPayload) {
            this.webhookUri = webhookUri;
            this.jsonPayload = jsonPayload;
            return new SendResult(204, "");
        }
    }
}
