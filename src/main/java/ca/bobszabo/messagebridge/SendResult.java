package ca.bobszabo.messagebridge;

/**
 * Result returned after a send attempt.
 *
 * @param statusCode HTTP-style status code returned by the delivery target
 * @param body response body returned by the delivery target
 */
public record SendResult(int statusCode, String body) {
    /**
     * Reports whether the status code is in the 2xx range.
     *
     * @return true when the status code indicates success
     */
    public boolean success() {
        return statusCode >= 200 && statusCode < 300;
    }
}
