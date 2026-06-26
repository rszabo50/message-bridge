package io.github.rszabo50.messagebridge.webhook;

/**
 * Chat platforms supported by the webhook sender.
 */
public enum ChatPlatform {
    /**
     * Slack incoming webhooks.
     */
    SLACK,

    /**
     * Mattermost incoming webhooks.
     */
    MATTERMOST,

    /**
     * Discord webhooks.
     */
    DISCORD,

    /**
     * Microsoft Teams workflow or webhook endpoints.
     */
    MICROSOFT_TEAMS
}
