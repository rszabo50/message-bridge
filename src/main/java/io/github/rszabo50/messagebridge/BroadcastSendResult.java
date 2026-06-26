package io.github.rszabo50.messagebridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Per-destination result of a broadcast send attempt.
 */
public final class BroadcastSendResult {
    private final List<DestinationOutcome> outcomes;

    /**
     * Creates a broadcast result from ordered destination outcomes.
     *
     * @param outcomes send outcomes ordered like the broadcast destinations
     */
    public BroadcastSendResult(List<DestinationOutcome> outcomes) {
        this.outcomes = java.util.Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(outcomes, "outcomes")));
    }

    static Builder builder() {
        return new Builder();
    }

    /**
     * Returns send outcomes ordered like the broadcast destinations.
     *
     * @return per-destination outcomes
     */
    public List<DestinationOutcome> outcomes() {
        return outcomes;
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
     */
    public static final class DestinationOutcome {
        private final int destinationIndex;
        private final SendResult result;
        private final Exception failure;

        /**
         * Creates a destination outcome.
         *
         * @param destinationIndex zero-based destination position
         * @param result send result when transport returned a response
         * @param failure exception thrown while sending, if any
         */
        public DestinationOutcome(int destinationIndex, SendResult result, Exception failure) {
            this.destinationIndex = destinationIndex;
            this.result = result;
            this.failure = failure;
        }

        /**
         * Returns the zero-based destination position.
         *
         * @return destination position
         */
        public int destinationIndex() {
            return destinationIndex;
        }

        /**
         * Returns the send result when transport returned a response.
         *
         * @return send result, or null when sending failed before a response
         */
        public SendResult result() {
            return result;
        }

        /**
         * Returns the failure thrown while sending.
         *
         * @return failure, or null when the destination returned a response
         */
        public Exception failure() {
            return failure;
        }

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
