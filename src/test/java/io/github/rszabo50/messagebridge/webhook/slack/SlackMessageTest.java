package io.github.rszabo50.messagebridge.webhook.slack;

import io.github.rszabo50.messagebridge.MessageBridge;
import io.github.rszabo50.messagebridge.MessageSender;
import io.github.rszabo50.messagebridge.OutboundMessage;
import io.github.rszabo50.messagebridge.SendResult;
import io.github.rszabo50.messagebridge.webhook.ChatPlatform;
import io.github.rszabo50.messagebridge.webhook.WebhookClient;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlackMessageTest {
    @Test
    void createsMarkdownSectionBlocks() {
        OutboundMessage message = SlackMessage.builder("Build failed")
                .markdownSection("*Build failed*")
                .divider()
                .markdownFields("*Project*\nmessage-bridge", "*Branch*\nmain")
                .markdownContext("ci smoke")
                .build();

        assertEquals("Build failed", message.text());

        List<?> blocks = (List<?>) message.platformOverrides().get("blocks");
        assertEquals(4, blocks.size());
        assertEquals("section", ((Map<?, ?>) blocks.get(0)).get("type"));
        assertEquals("divider", ((Map<?, ?>) blocks.get(1)).get("type"));
        assertEquals("section", ((Map<?, ?>) blocks.get(2)).get("type"));
        assertEquals("context", ((Map<?, ?>) blocks.get(3)).get("type"));
    }

    @Test
    void createsSlackWebhookJsonPayload() throws Exception {
        CapturingWebhookClient webhookClient = new CapturingWebhookClient();
        MessageSender sender = MessageBridge.webhookSender(
                ChatPlatform.SLACK,
                URI.create("https://hooks.slack.test/services/example"),
                webhookClient);

        sender.send(SlackMessage.markdownSection("Deployment started", "*Deployment started*"));

        assertEquals(
                "{\"text\":\"Deployment started\",\"blocks\":[{\"type\":\"section\",\"text\":{\"type\":\"mrkdwn\","
                        + "\"text\":\"*Deployment started*\"}}]}",
                webhookClient.jsonPayload);
    }

    @Test
    void rejectsBlankFallbackText() {
        assertThrows(IllegalArgumentException.class, () -> SlackMessage.builder(" "));
    }

    @Test
    void rejectsBlankBlockText() {
        SlackMessage.Builder builder = SlackMessage.builder("Build failed");

        assertThrows(IllegalArgumentException.class, () -> builder.markdownSection(""));
    }

    @Test
    void rejectsEmptyBuild() {
        SlackMessage.Builder builder = SlackMessage.builder("Build failed");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void rejectsMoreThanFiftyBlocks() {
        SlackMessage.Builder builder = SlackMessage.builder("Too many blocks");
        for (int index = 0; index < 50; index++) {
            builder.divider();
        }

        assertThrows(IllegalStateException.class, builder::divider);
    }

    @Test
    void returnsSuccessfulSendResult() throws Exception {
        CapturingWebhookClient webhookClient = new CapturingWebhookClient();
        MessageSender sender = MessageBridge.webhookSender(
                ChatPlatform.SLACK,
                URI.create("https://hooks.slack.test/services/example"),
                webhookClient);

        SendResult result = sender.send(SlackMessage.markdownSection("Build passed", "*Build passed*"));

        assertTrue(result.success());
    }

    private static final class CapturingWebhookClient implements WebhookClient {
        private String jsonPayload;

        @Override
        public SendResult postJson(URI webhookUri, String jsonPayload) {
            this.jsonPayload = jsonPayload;
            return new SendResult(204, "");
        }
    }
}
