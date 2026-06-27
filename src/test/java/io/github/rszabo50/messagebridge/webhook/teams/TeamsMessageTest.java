package io.github.rszabo50.messagebridge.webhook.teams;

import io.github.rszabo50.messagebridge.MessageBridge;
import io.github.rszabo50.messagebridge.MessageSender;
import io.github.rszabo50.messagebridge.OutboundMessage;
import io.github.rszabo50.messagebridge.SendResult;
import io.github.rszabo50.messagebridge.webhook.ChatPlatform;
import io.github.rszabo50.messagebridge.webhook.WebhookClient;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamsMessageTest {
    @Test
    void createsAdaptiveCardOverrides() {
        OutboundMessage message = TeamsMessage.builder("Build failed")
                .heading("Build failed")
                .textBlock("The main branch build failed.", true)
                .facts(Collections.singletonMap("Project", "message-bridge"))
                .openUrlAction("Open build", "https://example.test/builds/1")
                .build();

        assertEquals("Build failed", message.text());
        assertEquals("message", message.platformOverrides().get("type"));

        List<?> attachments = (List<?>) message.platformOverrides().get("attachments");
        Map<?, ?> attachment = (Map<?, ?>) attachments.get(0);
        Map<?, ?> content = (Map<?, ?>) attachment.get("content");
        assertEquals("application/vnd.microsoft.card.adaptive", attachment.get("contentType"));
        assertEquals("AdaptiveCard", content.get("type"));
        assertEquals(3, ((List<?>) content.get("body")).size());
        assertEquals(1, ((List<?>) content.get("actions")).size());
    }

    @Test
    void createsTeamsAdaptiveCardWebhookJsonPayload() throws Exception {
        CapturingWebhookClient webhookClient = new CapturingWebhookClient();
        MessageSender sender = MessageBridge.webhookSender(
                ChatPlatform.MICROSOFT_TEAMS,
                URI.create("https://teams.test/webhook/example"),
                webhookClient);

        sender.send(TeamsMessage.adaptiveText("Deployment started", "Production deploy is running."));

        assertFalse(webhookClient.jsonPayload.contains("\"@type\""));
        assertFalse(webhookClient.jsonPayload.contains("\"@context\""));
        assertEquals(
                "{\"type\":\"message\",\"attachments\":[{\"contentType\":\"application/vnd.microsoft.card.adaptive\","
                        + "\"contentUrl\":null,\"content\":{\"$schema\":\"http://adaptivecards.io/schemas/adaptive-card.json\","
                        + "\"type\":\"AdaptiveCard\",\"version\":\"1.2\",\"body\":[{\"type\":\"TextBlock\","
                        + "\"text\":\"Production deploy is running.\",\"wrap\":false}]}}]}",
                webhookClient.jsonPayload);
    }

    @Test
    void rejectsBlankSummary() {
        assertThrows(IllegalArgumentException.class, () -> TeamsMessage.builder(" "));
    }

    @Test
    void rejectsEmptyBuild() {
        TeamsMessage.Builder builder = TeamsMessage.builder("Build failed");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void rejectsEmptyFacts() {
        TeamsMessage.Builder builder = TeamsMessage.builder("Build failed");

        assertThrows(IllegalArgumentException.class, () -> builder.facts(Collections.emptyMap()));
    }

    @Test
    void returnsSuccessfulSendResult() throws Exception {
        CapturingWebhookClient webhookClient = new CapturingWebhookClient();
        MessageSender sender = MessageBridge.webhookSender(
                ChatPlatform.MICROSOFT_TEAMS,
                URI.create("https://teams.test/webhook/example"),
                webhookClient);

        SendResult result = sender.send(TeamsMessage.adaptiveText(
                "Build passed",
                "The main branch build passed."));

        assertTrue(result.success());
    }

    private static final class CapturingWebhookClient implements WebhookClient {
        private String jsonPayload;

        @Override
        public SendResult postJson(URI webhookUri, String jsonPayload) {
            this.jsonPayload = jsonPayload;
            return new SendResult(202, "");
        }
    }
}
