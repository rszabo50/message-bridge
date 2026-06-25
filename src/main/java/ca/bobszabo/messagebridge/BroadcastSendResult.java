package ca.bobszabo.messagebridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Per-destination result of a broadcast send attempt.
 *
 * @param outcomes send outcomes ordered like the broadcast destinations
 */
public record BroadcastSendResult(List<DestinationOutcome> outcomes) {
    public BroadcastSendResult {
        outcomes = List.copyOf(Objects.requireNonNull(outcomes, "outcomes"));
    }

    static Builder builder() {
        return new Builder();
    }

    /**
     * Reports whether every destination accepted the message.
     *
     * @return true when all destination outcomes succeeded
     */
    public boolean success() {
        return outcomes.stream().allMatch(DestinationOutcome::success);
    }

    /**
     * Outcome for one broadcast destination.
     *
     * @param destinationIndex zero-based destination position
     * @param result send result when transport returned a response
     * @param failure exception thrown while sending, if any
     */
    public record DestinationOutcome(int destinationIndex, SendResult result, Exception failure) {
        /**
         * Reports whether this destination accepted the message.
         *
         * @return true when the destination returned a successful result
         */
        public boolean success() {
            return failure == null && result != null && result.success();
        }
    }

    static final class Builder {
        private final List<DestinationOutcome> outcomes = new ArrayList<>();

        void add(int destinationIndex, SendResult result, Exception failure) {
            outcomes.add(new DestinationOutcome(destinationIndex, result, failure));
        }

        BroadcastSendResult build() {
            return new BroadcastSendResult(outcomes);
        }
    }
}
