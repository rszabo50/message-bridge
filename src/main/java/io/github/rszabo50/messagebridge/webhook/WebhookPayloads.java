package io.github.rszabo50.messagebridge.webhook;

import io.github.rszabo50.messagebridge.OutboundMessage;
import io.github.rszabo50.messagebridge.internal.JsonWriter;

import java.util.LinkedHashMap;
import java.util.Map;

final class WebhookPayloads {
    private WebhookPayloads() {
    }

    static String toJson(ChatPlatform platform, OutboundMessage message) {
        Map<String, Object> payload;
        switch (platform) {
            case SLACK:
            case MATTERMOST:
                payload = textPayload(message);
                break;
            case DISCORD:
                payload = discordPayload(message);
                break;
            case MICROSOFT_TEAMS:
                payload = teamsMessageCardPayload(message);
                break;
            default:
                throw new IllegalArgumentException("unsupported chat platform: " + platform);
        }

        payload.putAll(message.platformOverrides());
        return JsonWriter.write(payload);
    }

    private static Map<String, Object> textPayload(OutboundMessage message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("text", message.text());
        return payload;
    }

    private static Map<String, Object> discordPayload(OutboundMessage message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("content", message.text());
        return payload;
    }

    private static Map<String, Object> teamsMessageCardPayload(OutboundMessage message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("@type", "MessageCard");
        payload.put("@context", "https://schema.org/extensions");
        payload.put("summary", message.text());
        payload.put("text", message.text());
        return payload;
    }
}
