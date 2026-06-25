package io.github.rszabo50.messagebridge;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Sends the same message to multiple destinations.
 */
public final class BroadcastMessageSender {
    private final List<MessageSender> destinations;

    public BroadcastMessageSender(List<MessageSender> destinations) {
        Objects.requireNonNull(destinations, "destinations");
        if (destinations.isEmpty()) {
            throw new IllegalArgumentException("destinations must not be empty");
        }
        this.destinations = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(destinations));
    }

    /**
     * Sends one message to each destination.
     *
     * <p>Every destination is attempted even when an earlier destination fails.</p>
     *
     * @param message the message to send
     * @return per-destination send outcomes
     */
    public BroadcastSendResult send(OutboundMessage message) {
        Objects.requireNonNull(message, "message");

        BroadcastSendResult.Builder result = BroadcastSendResult.builder();
        for (int index = 0; index < destinations.size(); index++) {
            try {
                result.add(index, destinations.get(index).send(message), null);
            } catch (IOException exception) {
                result.add(index, null, exception);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                result.add(index, null, exception);
            } catch (RuntimeException exception) {
                result.add(index, null, exception);
            }
        }
        return result.build();
    }
}
