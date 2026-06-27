package io.github.rszabo50.messagebridge.webhook.discord;

import io.github.rszabo50.messagebridge.OutboundMessage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder utilities for Discord webhook messages.
 *
 * <p>The returned {@link OutboundMessage} keeps Discord fallback content and
 * adds Discord embed fields as platform overrides.</p>
 */
public final class DiscordMessage {
    private static final int MAX_EMBEDS = 10;
    private static final int MAX_EMBED_FIELDS = 25;

    private DiscordMessage() {
    }

    /**
     * Starts a Discord message builder.
     *
     * @param content fallback message content
     * @return a Discord message builder
     */
    public static Builder builder(String content) {
        return new Builder(content);
    }

    /**
     * Creates a Discord message with one embed containing a title and description.
     *
     * @param content fallback message content
     * @param title embed title
     * @param description embed description
     * @return an outbound message with a Discord embed
     */
    public static OutboundMessage embed(String content, String title, String description) {
        return builder(content)
                .embed(embed -> embed
                        .title(title)
                        .description(description))
                .build();
    }

    /**
     * Customizes one Discord embed.
     */
    @FunctionalInterface
    public interface EmbedCustomizer {
        /**
         * Applies embed configuration.
         *
         * @param embed embed builder
         */
        void customize(EmbedBuilder embed);
    }

    /**
     * Fluent builder for Discord webhook messages.
     */
    public static final class Builder {
        private final String content;
        private final List<Map<String, Object>> embeds = new ArrayList<>();

        private Builder(String content) {
            this.content = requireText(content, "content");
        }

        /**
         * Adds one embed.
         *
         * @param customizer embed customizer
         * @return this builder
         */
        public Builder embed(EmbedCustomizer customizer) {
            if (customizer == null) {
                throw new IllegalArgumentException("customizer must not be null");
            }
            if (embeds.size() == MAX_EMBEDS) {
                throw new IllegalStateException("Discord webhook messages support at most 10 embeds");
            }

            EmbedBuilder embed = new EmbedBuilder();
            customizer.customize(embed);
            embeds.add(embed.build());
            return this;
        }

        /**
         * Builds the outbound message.
         *
         * @return an outbound message with Discord embed overrides
         */
        public OutboundMessage build() {
            if (embeds.isEmpty()) {
                throw new IllegalStateException("at least one Discord embed is required");
            }

            return OutboundMessage.builder(content)
                    .platformOverride("embeds", Collections.unmodifiableList(new ArrayList<>(embeds)))
                    .build();
        }
    }

    /**
     * Fluent builder for one Discord embed object.
     */
    public static final class EmbedBuilder {
        private final Map<String, Object> embed = new LinkedHashMap<>();
        private final List<Map<String, Object>> fields = new ArrayList<>();

        private EmbedBuilder() {
        }

        /**
         * Sets the embed title.
         *
         * @param title embed title
         * @return this builder
         */
        public EmbedBuilder title(String title) {
            embed.put("title", requireText(title, "title"));
            return this;
        }

        /**
         * Sets the embed description.
         *
         * @param description embed description
         * @return this builder
         */
        public EmbedBuilder description(String description) {
            embed.put("description", requireText(description, "description"));
            return this;
        }

        /**
         * Sets the embed URL.
         *
         * @param url embed URL
         * @return this builder
         */
        public EmbedBuilder url(String url) {
            embed.put("url", requireText(url, "url"));
            return this;
        }

        /**
         * Sets the embed color as a decimal RGB integer.
         *
         * @param color decimal RGB color
         * @return this builder
         */
        public EmbedBuilder color(int color) {
            if (color < 0 || color > 0xFFFFFF) {
                throw new IllegalArgumentException("color must be between 0x000000 and 0xFFFFFF");
            }
            embed.put("color", color);
            return this;
        }

        /**
         * Sets the embed timestamp.
         *
         * @param timestamp timestamp to serialize as ISO-8601 text
         * @return this builder
         */
        public EmbedBuilder timestamp(Instant timestamp) {
            if (timestamp == null) {
                throw new IllegalArgumentException("timestamp must not be null");
            }
            embed.put("timestamp", timestamp.toString());
            return this;
        }

        /**
         * Adds a non-inline embed field.
         *
         * @param name field name
         * @param value field value
         * @return this builder
         */
        public EmbedBuilder field(String name, String value) {
            return field(name, value, false);
        }

        /**
         * Adds an embed field.
         *
         * @param name field name
         * @param value field value
         * @param inline whether Discord may render the field inline
         * @return this builder
         */
        public EmbedBuilder field(String name, String value, boolean inline) {
            if (fields.size() == MAX_EMBED_FIELDS) {
                throw new IllegalStateException("Discord embeds support at most 25 fields");
            }

            Map<String, Object> field = new LinkedHashMap<>();
            field.put("name", requireText(name, "name"));
            field.put("value", requireText(value, "value"));
            field.put("inline", inline);
            fields.add(Collections.unmodifiableMap(field));
            return this;
        }

        /**
         * Sets embed footer text.
         *
         * @param text footer text
         * @return this builder
         */
        public EmbedBuilder footer(String text) {
            return footer(text, null);
        }

        /**
         * Sets embed footer text and icon URL.
         *
         * @param text footer text
         * @param iconUrl footer icon URL
         * @return this builder
         */
        public EmbedBuilder footer(String text, String iconUrl) {
            Map<String, Object> footer = new LinkedHashMap<>();
            footer.put("text", requireText(text, "text"));
            putIfPresent(footer, "icon_url", iconUrl);
            embed.put("footer", Collections.unmodifiableMap(footer));
            return this;
        }

        /**
         * Sets embed author name.
         *
         * @param name author name
         * @return this builder
         */
        public EmbedBuilder author(String name) {
            return author(name, null, null);
        }

        /**
         * Sets embed author details.
         *
         * @param name author name
         * @param url author URL
         * @param iconUrl author icon URL
         * @return this builder
         */
        public EmbedBuilder author(String name, String url, String iconUrl) {
            Map<String, Object> author = new LinkedHashMap<>();
            author.put("name", requireText(name, "name"));
            putIfPresent(author, "url", url);
            putIfPresent(author, "icon_url", iconUrl);
            embed.put("author", Collections.unmodifiableMap(author));
            return this;
        }

        /**
         * Sets embed image URL.
         *
         * @param url image URL
         * @return this builder
         */
        public EmbedBuilder image(String url) {
            embed.put("image", urlObject(url));
            return this;
        }

        /**
         * Sets embed thumbnail URL.
         *
         * @param url thumbnail URL
         * @return this builder
         */
        public EmbedBuilder thumbnail(String url) {
            embed.put("thumbnail", urlObject(url));
            return this;
        }

        private Map<String, Object> build() {
            if (embed.isEmpty() && fields.isEmpty()) {
                throw new IllegalStateException("at least one Discord embed field is required");
            }

            Map<String, Object> built = new LinkedHashMap<>(embed);
            if (!fields.isEmpty()) {
                built.put("fields", Collections.unmodifiableList(new ArrayList<>(fields)));
            }
            return Collections.unmodifiableMap(built);
        }
    }

    private static Map<String, Object> urlObject(String url) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("url", requireText(url, "url"));
        return Collections.unmodifiableMap(value);
    }

    private static void putIfPresent(Map<String, Object> target, String name, String value) {
        if (value != null) {
            target.put(name, requireText(value, name));
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
