package io.github.rszabo50.messagebridge.webhook.slack;

import io.github.rszabo50.messagebridge.OutboundMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builder utilities for Slack incoming webhook messages.
 *
 * <p>The returned {@link OutboundMessage} keeps the portable fallback text and
 * adds Slack Block Kit fields as platform overrides.</p>
 */
public final class SlackMessage {
    private static final int MAX_MESSAGE_BLOCKS = 50;

    private SlackMessage() {
    }

    /**
     * Starts a Slack message builder.
     *
     * @param fallbackText fallback text used by Slack notifications and clients
     * @return a Slack message builder
     */
    public static Builder builder(String fallbackText) {
        return new Builder(fallbackText);
    }

    /**
     * Creates a Slack message with one markdown section block.
     *
     * @param fallbackText fallback text used by Slack notifications and clients
     * @param markdown section markdown text
     * @return an outbound message with Slack blocks
     */
    public static OutboundMessage markdownSection(String fallbackText, String markdown) {
        return builder(fallbackText).markdownSection(markdown).build();
    }

    /**
     * Fluent builder for common Slack Block Kit message blocks.
     */
    public static final class Builder {
        private final String fallbackText;
        private final List<Map<String, Object>> blocks = new ArrayList<>();

        private Builder(String fallbackText) {
            if (fallbackText == null || fallbackText.isBlank()) {
                throw new IllegalArgumentException("fallbackText must not be blank");
            }
            this.fallbackText = fallbackText;
        }

        /**
         * Adds a section block with Slack markdown text.
         *
         * @param markdown markdown text
         * @return this builder
         */
        public Builder markdownSection(String markdown) {
            return textSection("mrkdwn", markdown);
        }

        /**
         * Adds a section block with plain text.
         *
         * @param text plain text
         * @return this builder
         */
        public Builder plainTextSection(String text) {
            return textSection("plain_text", text);
        }

        /**
         * Adds one section block containing markdown fields.
         *
         * @param markdownFields field markdown values
         * @return this builder
         */
        public Builder markdownFields(String... markdownFields) {
            Objects.requireNonNull(markdownFields, "markdownFields");
            if (markdownFields.length == 0) {
                throw new IllegalArgumentException("markdownFields must not be empty");
            }

            List<Map<String, Object>> fields = new ArrayList<>();
            for (String markdownField : markdownFields) {
                fields.add(textObject("mrkdwn", requireText(markdownField, "markdownField")));
            }

            Map<String, Object> block = block("section");
            block.put("fields", Collections.unmodifiableList(fields));
            return addBlock(block);
        }

        /**
         * Adds a divider block.
         *
         * @return this builder
         */
        public Builder divider() {
            return addBlock(block("divider"));
        }

        /**
         * Adds a context block containing one markdown text element.
         *
         * @param markdown context markdown text
         * @return this builder
         */
        public Builder markdownContext(String markdown) {
            List<Map<String, Object>> elements = new ArrayList<>();
            elements.add(textObject("mrkdwn", requireText(markdown, "markdown")));

            Map<String, Object> block = block("context");
            block.put("elements", Collections.unmodifiableList(elements));
            return addBlock(block);
        }

        /**
         * Builds the outbound message.
         *
         * @return an outbound message with Slack Block Kit overrides
         */
        public OutboundMessage build() {
            if (blocks.isEmpty()) {
                throw new IllegalStateException("at least one Slack block is required");
            }

            return OutboundMessage.builder(fallbackText)
                    .platformOverride("blocks", Collections.unmodifiableList(new ArrayList<>(blocks)))
                    .build();
        }

        private Builder textSection(String type, String text) {
            Map<String, Object> block = block("section");
            block.put("text", textObject(type, requireText(text, "text")));
            return addBlock(block);
        }

        private Builder addBlock(Map<String, Object> block) {
            if (blocks.size() == MAX_MESSAGE_BLOCKS) {
                throw new IllegalStateException("Slack messages support at most 50 blocks");
            }
            blocks.add(Collections.unmodifiableMap(new LinkedHashMap<>(block)));
            return this;
        }
    }

    private static Map<String, Object> block(String type) {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("type", type);
        return block;
    }

    private static Map<String, Object> textObject(String type, String text) {
        Map<String, Object> textObject = new LinkedHashMap<>();
        textObject.put("type", type);
        textObject.put("text", text);
        return Collections.unmodifiableMap(textObject);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
