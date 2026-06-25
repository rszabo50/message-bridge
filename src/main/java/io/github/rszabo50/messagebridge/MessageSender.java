package io.github.rszabo50.messagebridge;

import java.io.IOException;

/**
 * Sends outbound messages through a concrete delivery mechanism.
 */
@FunctionalInterface
public interface MessageSender {
    /**
     * Sends one outbound message.
     *
     * @param message the message to send
     * @return the delivery result
     * @throws IOException when transport fails
     * @throws InterruptedException when the sending thread is interrupted
     */
    SendResult send(OutboundMessage message) throws IOException, InterruptedException;
}
