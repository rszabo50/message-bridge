# message-bridge

`message-bridge` is a small Java 21 library for sending outbound messages to chat platforms through incoming webhooks.

It is intentionally a library, not an application. There is no CLI, server runtime, framework bootstrap, or platform SDK dependency.

## Supported Webhook Targets

- Slack
- Mattermost
- Discord
- Microsoft Teams

## Install

```xml
<dependency>
    <groupId>ca.bobszabo</groupId>
    <artifactId>message-bridge</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Send A Message

```java
import ca.bobszabo.messagebridge.MessageBridge;
import ca.bobszabo.messagebridge.MessageSender;
import ca.bobszabo.messagebridge.OutboundMessage;
import ca.bobszabo.messagebridge.webhook.ChatPlatform;

import java.net.URI;

MessageSender sender = MessageBridge.webhookSender(
        ChatPlatform.SLACK,
        URI.create(System.getenv("SLACK_WEBHOOK_URL")));

sender.send(OutboundMessage.text("Build passed"));
```

## Platform Overrides

The portable model starts with text. Platform-specific webhook fields can be added as top-level JSON overrides.

```java
OutboundMessage message = OutboundMessage.text("Deployment started")
        .withPlatformOverride("username", "release-bot");
```

Discord allowed mentions:

```java
OutboundMessage message = OutboundMessage.text("@here Deployment started")
        .withPlatformOverride("allowed_mentions", Map.of("parse", List.of()));
```

Mattermost channel override:

```java
OutboundMessage message = OutboundMessage.text("Review requested")
        .withPlatformOverride("channel", "engineering");
```

Teams MessageCard fields:

```java
OutboundMessage message = OutboundMessage.text("Incident resolved")
        .withPlatformOverride("themeColor", "2EB67D");
```

Supported override values are JSON-compatible values: strings, numbers, booleans, nulls, maps with string keys, iterables, and arrays.

## Design Notes

- Java 21 baseline.
- Runtime dependencies: none.
- HTTP transport: Java `HttpClient`.
- Webhook URLs are secrets; load them from environment variables or your application's secret store.
- Richer typed builders can be added later without forcing platform SDKs into the core library.

## Real Webhook Integration Tests

Normal unit tests do not call the network:

```shell
mvn test
```

Real webhook tests are opt-in through the `webhook-integration` profile:

```shell
MESSAGE_BRIDGE_SLACK_WEBHOOK_URL="https://hooks.slack.com/services/..." \
MESSAGE_BRIDGE_MATTERMOST_WEBHOOK_URL="https://mattermost.example/hooks/..." \
MESSAGE_BRIDGE_DISCORD_WEBHOOK_URL="https://discord.com/api/webhooks/..." \
MESSAGE_BRIDGE_TEAMS_WEBHOOK_URL="https://..." \
mvn -Pwebhook-integration verify
```

Each variable is optional. Tests without a matching environment variable are skipped.
