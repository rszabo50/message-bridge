package io.github.rszabo50.messagebridge.webhook.discord;

import io.github.rszabo50.messagebridge.MessageBridge;
import io.github.rszabo50.messagebridge.MessageSender;
import io.github.rszabo50.messagebridge.OutboundMessage;
import io.github.rszabo50.messagebridge.SendResult;
import io.github.rszabo50.messagebridge.webhook.ChatPlatform;
import io.github.rszabo50.messagebridge.webhook.WebhookClient;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscordMessageTest {
    @Test
    void createsEmbedOverrides() {
        OutboundMessage message = DiscordMessage.builder("Build failed")
                .embed(embed -> embed
                        .title("Build failed")
                        .description("The main branch build failed.")
                        .color(0xE01E5A)
                        .field("Project", "message-bridge", true)
                        .field("Branch", "main", true)
                        .footer("ci"))
                .build();

        assertEquals("Build failed", message.text());

        List<?> embeds = (List<?>) message.platformOverrides().get("embeds");
        assertEquals(1, embeds.size());
        Map<?, ?> embed = (Map<?, ?>) embeds.get(0);
        assertEquals("Build failed", embed.get("title"));
        assertEquals(0xE01E5A, embed.get("color"));
        assertEquals(2, ((List<?>) embed.get("fields")).size());
    }

    @Test
    void createsDiscordWebhookJsonPayload() throws Exception {
        CapturingWebhookClient webhookClient = new CapturingWebhookClient();
        MessageSender sender = MessageBridge.webhookSender(
                ChatPlatform.DISCORD,
                URI.create("https://discord.test/api/webhooks/example"),
                webhookClient);

        sender.send(DiscordMessage.builder("Deployment started")
                .embed(embed -> embed
                        .title("Deployment started")
                        .description("Production deploy is running.")
                        .color(0x2EB67D))
                .build());

        assertEquals(
                "{\"content\":\"Deployment started\",\"embeds\":[{\"title\":\"Deployment started\","
                        + "\"description\":\"Production deploy is running.\",\"color\":3061373}]}",
                webhookClient.jsonPayload);
    }

    @Test
    void supportsCommonEmbedFields() {
        Instant timestamp = Instant.parse("2026-06-27T11:45:00Z");

        OutboundMessage message = DiscordMessage.builder("Incident resolved")
                .embed(embed -> embed
                        .author("message-bridge", "https://example.test", "https://example.test/icon.png")
                        .title("Incident resolved")
                        .url("https://example.test/incidents/1")
                        .thumbnail("https://example.test/thumb.png")
                        .image("https://example.test/image.png")
                        .timestamp(timestamp)
                        .footer("ops", "https://example.test/footer.png"))
                .build();

        Map<?, ?> embed = (Map<?, ?>) ((List<?>) message.platformOverrides().get("embeds")).get(0);

        assertEquals("2026-06-27T11:45:00Z", embed.get("timestamp"));
        assertEquals("https://example.test/thumb.png", ((Map<?, ?>) embed.get("thumbnail")).get("url"));
        assertEquals("ops", ((Map<?, ?>) embed.get("footer")).get("text"));
    }

    @Test
    void rejectsBlankContent() {
        assertThrows(IllegalArgumentException.class, () -> DiscordMessage.builder(" "));
    }

    @Test
    void rejectsEmptyBuild() {
        DiscordMessage.Builder builder = DiscordMessage.builder("Build failed");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void rejectsEmptyEmbed() {
        DiscordMessage.Builder builder = DiscordMessage.builder("Build failed");

        assertThrows(IllegalStateException.class, () -> builder.embed(embed -> {
        }));
    }

    @Test
    void rejectsMoreThanTenEmbeds() {
        DiscordMessage.Builder builder = DiscordMessage.builder("Too many embeds");
        for (int index = 0; index < 10; index++) {
            final int embedIndex = index;
            builder.embed(embed -> embed.title("Embed " + embedIndex));
        }

        assertThrows(IllegalStateException.class, () -> builder.embed(embed -> embed.title("Overflow")));
    }

    @Test
    void rejectsMoreThanTwentyFiveFields() {
        DiscordMessage.Builder builder = DiscordMessage.builder("Too many fields");

        assertThrows(IllegalStateException.class, () -> builder.embed(embed -> {
            for (int index = 0; index < 25; index++) {
                embed.field("Field " + index, "Value " + index);
            }
            embed.field("Overflow", "Value");
        }));
    }

    @Test
    void returnsSuccessfulSendResult() throws Exception {
        CapturingWebhookClient webhookClient = new CapturingWebhookClient();
        MessageSender sender = MessageBridge.webhookSender(
                ChatPlatform.DISCORD,
                URI.create("https://discord.test/api/webhooks/example"),
                webhookClient);

        SendResult result = sender.send(DiscordMessage.embed(
                "Build passed",
                "Build passed",
                "The main branch build passed."));

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
