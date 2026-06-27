package io.github.rszabo50.messagebridge.webhook.teams;

import io.github.rszabo50.messagebridge.OutboundMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder utilities for Microsoft Teams webhook messages.
 *
 * <p>The returned {@link OutboundMessage} keeps the fallback summary and adds
 * Teams Adaptive Card payload fields as platform overrides.</p>
 */
public final class TeamsMessage {
    private static final String ADAPTIVE_CARD_CONTENT_TYPE = "application/vnd.microsoft.card.adaptive";
    private static final String ADAPTIVE_CARD_SCHEMA = "http://adaptivecards.io/schemas/adaptive-card.json";

    private TeamsMessage() {
    }

    /**
     * Starts a Teams Adaptive Card message builder.
     *
     * @param summary fallback summary text
     * @return a Teams message builder
     */
    public static Builder builder(String summary) {
        return new Builder(summary);
    }

    /**
     * Creates a Teams Adaptive Card message with one text block.
     *
     * @param summary fallback summary text
     * @param text text block content
     * @return an outbound message with a Teams Adaptive Card payload
     */
    public static OutboundMessage adaptiveText(String summary, String text) {
        return builder(summary).textBlock(text).build();
    }

    /**
     * Fluent builder for a Teams Adaptive Card webhook payload.
     */
    public static final class Builder {
        private final String summary;
        private final List<Map<String, Object>> body = new ArrayList<>();
        private final List<Map<String, Object>> actions = new ArrayList<>();
        private String version = "1.2";

        private Builder(String summary) {
            this.summary = requireText(summary, "summary");
        }

        /**
         * Sets the Adaptive Card schema version.
         *
         * @param version Adaptive Card version
         * @return this builder
         */
        public Builder version(String version) {
            this.version = requireText(version, "version");
            return this;
        }

        /**
         * Adds a normal text block.
         *
         * @param text block text
         * @return this builder
         */
        public Builder textBlock(String text) {
            return textBlock(text, false);
        }

        /**
         * Adds a text block.
         *
         * @param text block text
         * @param wrap whether the text should wrap
         * @return this builder
         */
        public Builder textBlock(String text, boolean wrap) {
            Map<String, Object> block = new LinkedHashMap<>();
            block.put("type", "TextBlock");
            block.put("text", requireText(text, "text"));
            block.put("wrap", wrap);
            body.add(Collections.unmodifiableMap(block));
            return this;
        }

        /**
         * Adds a heading text block.
         *
         * @param text heading text
         * @return this builder
         */
        public Builder heading(String text) {
            Map<String, Object> block = new LinkedHashMap<>();
            block.put("type", "TextBlock");
            block.put("text", requireText(text, "text"));
            block.put("weight", "Bolder");
            block.put("size", "Medium");
            block.put("wrap", true);
            body.add(Collections.unmodifiableMap(block));
            return this;
        }

        /**
         * Adds a fact set.
         *
         * @param facts facts to render as name/value pairs
         * @return this builder
         */
        public Builder facts(Map<String, String> facts) {
            if (facts == null || facts.isEmpty()) {
                throw new IllegalArgumentException("facts must not be empty");
            }

            List<Map<String, Object>> factValues = new ArrayList<>();
            for (Map.Entry<String, String> fact : facts.entrySet()) {
                Map<String, Object> factValue = new LinkedHashMap<>();
                factValue.put("title", requireText(fact.getKey(), "fact title"));
                factValue.put("value", requireText(fact.getValue(), "fact value"));
                factValues.add(Collections.unmodifiableMap(factValue));
            }

            Map<String, Object> block = new LinkedHashMap<>();
            block.put("type", "FactSet");
            block.put("facts", Collections.unmodifiableList(factValues));
            body.add(Collections.unmodifiableMap(block));
            return this;
        }

        /**
         * Adds an open URL action.
         *
         * @param title action title
         * @param url URL to open
         * @return this builder
         */
        public Builder openUrlAction(String title, String url) {
            Map<String, Object> action = new LinkedHashMap<>();
            action.put("type", "Action.OpenUrl");
            action.put("title", requireText(title, "title"));
            action.put("url", requireText(url, "url"));
            actions.add(Collections.unmodifiableMap(action));
            return this;
        }

        /**
         * Builds the outbound message.
         *
         * @return an outbound message with Teams Adaptive Card overrides
         */
        public OutboundMessage build() {
            if (body.isEmpty()) {
                throw new IllegalStateException("at least one Teams Adaptive Card body element is required");
            }

            Map<String, Object> content = new LinkedHashMap<>();
            content.put("$schema", ADAPTIVE_CARD_SCHEMA);
            content.put("type", "AdaptiveCard");
            content.put("version", version);
            content.put("body", Collections.unmodifiableList(new ArrayList<>(body)));
            if (!actions.isEmpty()) {
                content.put("actions", Collections.unmodifiableList(new ArrayList<>(actions)));
            }

            Map<String, Object> attachment = new LinkedHashMap<>();
            attachment.put("contentType", ADAPTIVE_CARD_CONTENT_TYPE);
            attachment.put("contentUrl", null);
            attachment.put("content", Collections.unmodifiableMap(content));

            List<Map<String, Object>> attachments = new ArrayList<>();
            attachments.add(Collections.unmodifiableMap(attachment));

            return OutboundMessage.builder(summary)
                    .platformOverride("type", "message")
                    .platformOverride("attachments", Collections.unmodifiableList(attachments))
                    .build();
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
