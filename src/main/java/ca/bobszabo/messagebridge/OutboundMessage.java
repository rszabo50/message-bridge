package ca.bobszabo.messagebridge;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record OutboundMessage(String text, Map<String, Object> platformOverrides) {
    public OutboundMessage {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        platformOverrides = Collections.unmodifiableMap(new LinkedHashMap<>(
                Objects.requireNonNull(platformOverrides, "platformOverrides")));
    }

    public static OutboundMessage text(String text) {
        return new OutboundMessage(text, Map.of());
    }

    public OutboundMessage withPlatformOverride(String name, Object value) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        Map<String, Object> overrides = new LinkedHashMap<>(platformOverrides);
        overrides.put(name, value);
        return new OutboundMessage(text, overrides);
    }
}
