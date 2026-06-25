package io.github.rszabo50.messagebridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Per-destination result of a broadcast send attempt.
 */
public final class BroadcastSendResult {
    private final List<DestinationOutcome> outcomes;

    public BroadcastSendResult(List<DestinationOutcome> outcomes) {
        this.outcomes = java.util.Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(outcomes, "outcomes")));
    }

    static Builder builder() {
        return new Builder();
    }

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

        public DestinationOutcome(int destinationIndex, SendResult result, Exception failure) {
            this.destinationIndex = destinationIndex;
            this.result = result;
            this.failure = failure;
        }

        public int destinationIndex() {
            return destinationIndex;
        }

        public SendResult result() {
            return result;
        }

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
