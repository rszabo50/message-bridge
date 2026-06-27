package io.github.rszabo50.messagebridge.webhook.mattermost;

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

class MattermostMessageTest {
    @Test
    void createsAttachmentOverrides() {
        OutboundMessage message = MattermostMessage.builder("Build failed")
                .attachment("Build failed", attachment -> attachment
                        .color(0xE01E5A)
                        .pretext("CI notification")
                        .text("The main branch build failed.")
                        .title("Build failed", "https://example.test/builds/1")
                        .field("Project", "message-bridge", true)
                        .field("Branch", "main", true)
                        .footer("ci"))
                .build();

        assertEquals("Build failed", message.text());

        List<?> attachments = (List<?>) message.platformOverrides().get("attachments");
        assertEquals(1, attachments.size());
        Map<?, ?> attachment = (Map<?, ?>) attachments.get(0);
        assertEquals("Build failed", attachment.get("fallback"));
        assertEquals("#E01E5A", attachment.get("color"));
        assertEquals(2, ((List<?>) attachment.get("fields")).size());
    }

    @Test
    void createsMattermostWebhookJsonPayload() throws Exception {
        CapturingWebhookClient webhookClient = new CapturingWebhookClient();
        MessageSender sender = MessageBridge.webhookSender(
                ChatPlatform.MATTERMOST,
                URI.create("https://mattermost.test/hooks/example"),
                webhookClient);

        sender.send(MattermostMessage.attachment(
                "Deployment started",
                "Deployment started",
                "Production deploy is running."));

        assertEquals(
                "{\"text\":\"Deployment started\",\"attachments\":[{\"fallback\":\"Deployment started\","
                        + "\"text\":\"Production deploy is running.\"}]}",
                webhookClient.jsonPayload);
    }

    @Test
    void supportsCommonAttachmentFields() {
        OutboundMessage message = MattermostMessage.builder("Incident resolved")
                .attachment("Incident resolved", attachment -> attachment
                        .author("message-bridge", "https://example.test", "https://example.test/icon.png")
                        .title("Incident resolved")
                        .color("#2EB67D")
                        .imageUrl("https://example.test/image.png")
                        .thumbnailUrl("https://example.test/thumb.png")
                        .footer("ops", "https://example.test/footer.png"))
                .build();

        Map<?, ?> attachment = (Map<?, ?>) ((List<?>) message.platformOverrides().get("attachments")).get(0);

        assertEquals("message-bridge", attachment.get("author_name"));
        assertEquals("#2EB67D", attachment.get("color"));
        assertEquals("https://example.test/thumb.png", attachment.get("thumb_url"));
        assertEquals("https://example.test/footer.png", attachment.get("footer_icon"));
    }

    @Test
    void rejectsBlankText() {
        assertThrows(IllegalArgumentException.class, () -> MattermostMessage.builder(" "));
    }

    @Test
    void rejectsBlankFallback() {
        MattermostMessage.Builder builder = MattermostMessage.builder("Build failed");

        assertThrows(IllegalArgumentException.class, () -> builder.attachment(" ", attachment -> attachment.text("x")));
    }

    @Test
    void rejectsInvalidColor() {
        MattermostMessage.Builder builder = MattermostMessage.builder("Build failed");

        assertThrows(IllegalArgumentException.class, () -> builder.attachment(
                "Build failed",
                attachment -> attachment.color("orange")));
    }

    @Test
    void rejectsEmptyBuild() {
        MattermostMessage.Builder builder = MattermostMessage.builder("Build failed");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void returnsSuccessfulSendResult() throws Exception {
        CapturingWebhookClient webhookClient = new CapturingWebhookClient();
        MessageSender sender = MessageBridge.webhookSender(
                ChatPlatform.MATTERMOST,
                URI.create("https://mattermost.test/hooks/example"),
                webhookClient);

        SendResult result = sender.send(MattermostMessage.attachment(
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
            return new SendResult(200, "ok");
        }
    }
}
