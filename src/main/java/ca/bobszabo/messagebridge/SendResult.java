package ca.bobszabo.messagebridge;

public record SendResult(int statusCode, String body) {
    public boolean success() {
        return statusCode >= 200 && statusCode < 300;
    }
}
