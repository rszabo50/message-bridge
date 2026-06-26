package io.github.rszabo50.messagebridge;

/**
 * Result returned after a send attempt.
 */
public final class SendResult {
    private final int statusCode;
    private final String body;

    /**
     * Creates a send result.
     *
     * @param statusCode HTTP-style status code returned by the delivery target
     * @param body response body returned by the delivery target
     */
    public SendResult(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    /**
     * Returns the HTTP-style status code.
     *
     * @return status code
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * Returns the response body.
     *
     * @return response body
     */
    public String body() {
        return body;
    }

    /**
     * Reports whether the status code is in the 2xx range.
     *
     * @return true when the status code indicates success
     */
    public boolean success() {
        return statusCode >= 200 && statusCode < 300;
    }
}
