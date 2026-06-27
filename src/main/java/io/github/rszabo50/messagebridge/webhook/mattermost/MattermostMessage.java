package io.github.rszabo50.messagebridge.webhook.mattermost;

import io.github.rszabo50.messagebridge.OutboundMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder utilities for Mattermost webhook messages.
 *
 * <p>The returned {@link OutboundMessage} keeps the message text and adds
 * Mattermost attachment fields as platform overrides.</p>
 */
public final class MattermostMessage {
    private MattermostMessage() {
    }

    /**
     * Starts a Mattermost message builder.
     *
     * @param text message text
     * @return a Mattermost message builder
     */
    public static Builder builder(String text) {
        return new Builder(text);
    }

    /**
     * Creates a Mattermost message with one text attachment.
     *
     * @param text message text
     * @param fallback attachment fallback text
     * @param attachmentText attachment body text
     * @return an outbound message with Mattermost attachments
     */
    public static OutboundMessage attachment(String text, String fallback, String attachmentText) {
        return builder(text)
                .attachment(fallback, attachment -> attachment.text(attachmentText))
                .build();
    }

    /**
     * Customizes one Mattermost attachment.
     */
    @FunctionalInterface
    public interface AttachmentCustomizer {
        /**
         * Applies attachment configuration.
         *
         * @param attachment attachment builder
         */
        void customize(AttachmentBuilder attachment);
    }

    /**
     * Fluent builder for Mattermost webhook messages.
     */
    public static final class Builder {
        private final String text;
        private final List<Map<String, Object>> attachments = new ArrayList<>();

        private Builder(String text) {
            this.text = requireText(text, "text");
        }

        /**
         * Adds one attachment.
         *
         * @param fallback plain-text attachment summary
         * @param customizer attachment customizer
         * @return this builder
         */
        public Builder attachment(String fallback, AttachmentCustomizer customizer) {
            if (customizer == null) {
                throw new IllegalArgumentException("customizer must not be null");
            }

            AttachmentBuilder attachment = new AttachmentBuilder(fallback);
            customizer.customize(attachment);
            attachments.add(attachment.build());
            return this;
        }

        /**
         * Builds the outbound message.
         *
         * @return an outbound message with Mattermost attachment overrides
         */
        public OutboundMessage build() {
            if (attachments.isEmpty()) {
                throw new IllegalStateException("at least one Mattermost attachment is required");
            }

            return OutboundMessage.builder(text)
                    .platformOverride("attachments", Collections.unmodifiableList(new ArrayList<>(attachments)))
                    .build();
        }
    }

    /**
     * Fluent builder for one Mattermost attachment object.
     */
    public static final class AttachmentBuilder {
        private final Map<String, Object> attachment = new LinkedHashMap<>();
        private final List<Map<String, Object>> fields = new ArrayList<>();

        private AttachmentBuilder(String fallback) {
            attachment.put("fallback", requireText(fallback, "fallback"));
        }

        /**
         * Sets the left-border color as a hex color code.
         *
         * @param color color such as {@code #FF8000} or {@code FF8000}
         * @return this builder
         */
        public AttachmentBuilder color(String color) {
            String normalized = requireText(color, "color");
            if (!normalized.matches("#?[0-9a-fA-F]{6}")) {
                throw new IllegalArgumentException("color must be a 6-digit hex color code");
            }
            if (!normalized.startsWith("#")) {
                normalized = "#" + normalized;
            }
            attachment.put("color", normalized.toUpperCase());
            return this;
        }

        /**
         * Sets the left-border color as an RGB integer.
         *
         * @param color decimal RGB color
         * @return this builder
         */
        public AttachmentBuilder color(int color) {
            if (color < 0 || color > 0xFFFFFF) {
                throw new IllegalArgumentException("color must be between 0x000000 and 0xFFFFFF");
            }
            attachment.put("color", String.format("#%06X", color));
            return this;
        }

        /**
         * Sets text displayed above the attachment.
         *
         * @param pretext pretext
         * @return this builder
         */
        public AttachmentBuilder pretext(String pretext) {
            attachment.put("pretext", requireText(pretext, "pretext"));
            return this;
        }

        /**
         * Sets attachment body text.
         *
         * @param text attachment text
         * @return this builder
         */
        public AttachmentBuilder text(String text) {
            attachment.put("text", requireText(text, "text"));
            return this;
        }

        /**
         * Sets author name.
         *
         * @param name author name
         * @return this builder
         */
        public AttachmentBuilder author(String name) {
            return author(name, null, null);
        }

        /**
         * Sets author details.
         *
         * @param name author name
         * @param link author link
         * @param icon author icon URL
         * @return this builder
         */
        public AttachmentBuilder author(String name, String link, String icon) {
            attachment.put("author_name", requireText(name, "name"));
            putIfPresent("author_link", link);
            putIfPresent("author_icon", icon);
            return this;
        }

        /**
         * Sets attachment title.
         *
         * @param title attachment title
         * @return this builder
         */
        public AttachmentBuilder title(String title) {
            attachment.put("title", requireText(title, "title"));
            return this;
        }

        /**
         * Sets attachment title and title link.
         *
         * @param title attachment title
         * @param titleLink title link
         * @return this builder
         */
        public AttachmentBuilder title(String title, String titleLink) {
            attachment.put("title", requireText(title, "title"));
            putIfPresent("title_link", titleLink);
            return this;
        }

        /**
         * Adds a long attachment field.
         *
         * @param title field title
         * @param value field value
         * @return this builder
         */
        public AttachmentBuilder field(String title, String value) {
            return field(title, value, false);
        }

        /**
         * Adds an attachment field.
         *
         * @param title field title
         * @param value field value
         * @param shortField whether Mattermost may render the field beside others
         * @return this builder
         */
        public AttachmentBuilder field(String title, String value, boolean shortField) {
            Map<String, Object> field = new LinkedHashMap<>();
            field.put("title", requireText(title, "title"));
            field.put("value", requireText(value, "value"));
            field.put("short", shortField);
            fields.add(Collections.unmodifiableMap(field));
            return this;
        }

        /**
         * Sets attachment image URL.
         *
         * @param imageUrl image URL
         * @return this builder
         */
        public AttachmentBuilder imageUrl(String imageUrl) {
            attachment.put("image_url", requireText(imageUrl, "imageUrl"));
            return this;
        }

        /**
         * Sets attachment thumbnail URL.
         *
         * @param thumbnailUrl thumbnail URL
         * @return this builder
         */
        public AttachmentBuilder thumbnailUrl(String thumbnailUrl) {
            attachment.put("thumb_url", requireText(thumbnailUrl, "thumbnailUrl"));
            return this;
        }

        /**
         * Sets footer text.
         *
         * @param footer footer text
         * @return this builder
         */
        public AttachmentBuilder footer(String footer) {
            attachment.put("footer", requireText(footer, "footer"));
            return this;
        }

        /**
         * Sets footer text and icon URL.
         *
         * @param footer footer text
         * @param footerIcon footer icon URL
         * @return this builder
         */
        public AttachmentBuilder footer(String footer, String footerIcon) {
            attachment.put("footer", requireText(footer, "footer"));
            putIfPresent("footer_icon", footerIcon);
            return this;
        }

        private Map<String, Object> build() {
            Map<String, Object> built = new LinkedHashMap<>(attachment);
            if (!fields.isEmpty()) {
                built.put("fields", Collections.unmodifiableList(new ArrayList<>(fields)));
            }
            return Collections.unmodifiableMap(built);
        }

        private void putIfPresent(String name, String value) {
            if (value != null) {
                attachment.put(name, requireText(value, name));
            }
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
