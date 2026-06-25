package ca.bobszabo.messagebridge;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BroadcastMessageSenderTest {
    @Test
    void sendsSameMessageToEveryDestination() {
        CapturingSender first = new CapturingSender(new SendResult(204, ""));
        CapturingSender second = new CapturingSender(new SendResult(200, "ok"));
        BroadcastMessageSender sender = MessageBridge.broadcast(List.of(first, second));

        BroadcastSendResult result = sender.send(OutboundMessage.text("Switching vendors"));

        assertTrue(result.success());
        assertEquals("Switching vendors", first.messages.getFirst().text());
        assertEquals("Switching vendors", second.messages.getFirst().text());
        assertEquals(2, result.outcomes().size());
    }

    @Test
    void attemptsRemainingDestinationsAfterFailure() {
        FailingSender first = new FailingSender(new IOException("old vendor unavailable"));
        CapturingSender second = new CapturingSender(new SendResult(204, ""));
        BroadcastMessageSender sender = MessageBridge.broadcast(List.of(first, second));

        BroadcastSendResult result = sender.send(OutboundMessage.text("Switching vendors"));

        assertFalse(result.success());
        assertEquals(1, second.messages.size());
        assertInstanceOf(IOException.class, result.outcomes().getFirst().failure());
        assertTrue(result.outcomes().get(1).success());
    }

    @Test
    void rejectsEmptyDestinations() {
        assertThrows(IllegalArgumentException.class, () -> MessageBridge.broadcast(List.of()));
    }

    private static final class CapturingSender implements MessageSender {
        private final SendResult result;
        private final List<OutboundMessage> messages = new ArrayList<>();

        private CapturingSender(SendResult result) {
            this.result = result;
        }

        @Override
        public SendResult send(OutboundMessage message) {
            messages.add(message);
            return result;
        }
    }

    private static final class FailingSender implements MessageSender {
        private final IOException failure;

        private FailingSender(IOException failure) {
            this.failure = failure;
        }

        @Override
        public SendResult send(OutboundMessage message) throws IOException {
            throw failure;
        }
    }
}
