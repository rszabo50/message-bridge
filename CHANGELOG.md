# Changelog

All notable changes to this project will be documented in this file.

## 0.1.0 - Unreleased

### Added

- Java 11 library for sending outbound webhook messages.
- Webhook support for Slack, Mattermost, Discord, and Microsoft Teams.
- Immutable outbound message model with platform-specific JSON overrides.
- Fluent `OutboundMessage.Builder` for composing messages with multiple overrides.
- Broadcast sender for sending the same message to multiple destinations during vendor transitions.
- Java `HttpClient` based webhook transport with no runtime dependencies.
- Opt-in real webhook integration tests.
- Maven release artifact profile for main, source, and Javadoc jars.
- Maven Central publishing profile and GitHub Actions release workflow.
- 0BSD license.
