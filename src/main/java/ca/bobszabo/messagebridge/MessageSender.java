package ca.bobszabo.messagebridge;

import java.io.IOException;

@FunctionalInterface
public interface MessageSender {
    SendResult send(OutboundMessage message) throws IOException, InterruptedException;
}
