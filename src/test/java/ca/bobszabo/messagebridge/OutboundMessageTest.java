package ca.bobszabo.messagebridge;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OutboundMessageTest {
    @Test
    void requiresText() {
        assertThrows(IllegalArgumentException.class, () -> OutboundMessage.text(" "));
    }

    @Test
    void addsPlatformOverridesImmutably() {
        OutboundMessage original = OutboundMessage.text("Hello");
        OutboundMessage changed = original.withPlatformOverride("username", "bridge");

        assertEquals(0, original.platformOverrides().size());
        assertEquals("bridge", changed.platformOverrides().get("username"));
    }

    @Test
    void requiresOverrideName() {
        OutboundMessage original = OutboundMessage.text("Hello");

        assertThrows(IllegalArgumentException.class, () -> original.withPlatformOverride("", "bridge"));
    }

    @Test
    void builderAddsMultiplePlatformOverrides() {
        OutboundMessage message = OutboundMessage.builder("Hello")
                .platformOverride("username", "bridge")
                .platformOverrides(Map.of("channel", "engineering"))
                .build();

        assertEquals("Hello", message.text());
        assertEquals("bridge", message.platformOverrides().get("username"));
        assertEquals("engineering", message.platformOverrides().get("channel"));
    }

    @Test
    void builderRequiresOverrideName() {
        OutboundMessage.Builder builder = OutboundMessage.builder("Hello");

        assertThrows(IllegalArgumentException.class, () -> builder.platformOverride(" ", "bridge"));
    }
}
