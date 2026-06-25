package io.github.rszabo50.messagebridge;

/**
 * Result returned after a send attempt.
 */
public final class SendResult {
    private final int statusCode;
    private final String body;

    public SendResult(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int statusCode() {
        return statusCode;
    }

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
