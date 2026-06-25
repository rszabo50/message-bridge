package ca.bobszabo.messagebridge;

import org.junit.jupiter.api.Test;

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
}
