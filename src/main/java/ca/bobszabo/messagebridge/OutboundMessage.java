package ca.bobszabo.messagebridge;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Portable outbound message model.
 *
 * <p>The core model is plain text. Platform-specific webhook JSON fields can be
 * supplied through {@link #platformOverrides()}.</p>
 *
 * @param text the message text
 * @param platformOverrides top-level platform-specific JSON fields
 */
public record OutboundMessage(String text, Map<String, Object> platformOverrides) {
    public OutboundMessage {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        platformOverrides = Collections.unmodifiableMap(new LinkedHashMap<>(
                Objects.requireNonNull(platformOverrides, "platformOverrides")));
    }

    /**
     * Creates a plain text outbound message.
     *
     * @param text the message text
     * @return an outbound message
     */
    public static OutboundMessage text(String text) {
        return new OutboundMessage(text, Map.of());
    }

    /**
     * Starts a fluent builder for an outbound message.
     *
     * @param text the message text
     * @return a message builder
     */
    public static Builder builder(String text) {
        return new Builder(text);
    }

    /**
     * Returns a copy with one additional platform-specific top-level JSON field.
     *
     * <p>Values must be JSON-compatible: strings, numbers, booleans, nulls,
     * maps with string keys, iterables, or arrays.</p>
     *
     * @param name the JSON field name
     * @param value the JSON field value
     * @return a new message with the override added
     */
    public OutboundMessage withPlatformOverride(String name, Object value) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        Map<String, Object> overrides = new LinkedHashMap<>(platformOverrides);
        overrides.put(name, value);
        return new OutboundMessage(text, overrides);
    }

    /**
     * Fluent builder for composing an outbound message with multiple platform overrides.
     */
    public static final class Builder {
        private final String text;
        private final Map<String, Object> platformOverrides = new LinkedHashMap<>();

        private Builder(String text) {
            this.text = text;
        }

        /**
         * Adds one platform-specific top-level JSON field.
         *
         * @param name the JSON field name
         * @param value the JSON field value
         * @return this builder
         */
        public Builder platformOverride(String name, Object value) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name must not be blank");
            }
            platformOverrides.put(name, value);
            return this;
        }

        /**
         * Adds platform-specific top-level JSON fields.
         *
         * @param values JSON field values keyed by field name
         * @return this builder
         */
        public Builder platformOverrides(Map<String, Object> values) {
            Objects.requireNonNull(values, "values");
            values.forEach(this::platformOverride);
            return this;
        }

        /**
         * Creates the immutable outbound message.
         *
         * @return an outbound message
         */
        public OutboundMessage build() {
            return new OutboundMessage(text, platformOverrides);
        }
    }
}
