# Publishing

Public repository publishing is configured as an opt-in Maven profile for Maven Central. The library can also be installed locally and can build release-style artifacts.

## Current Commands

Install the snapshot into the local Maven repository:

```shell
mvn install
```

Build the main, source, and Javadoc jars:

```shell
mvn -Prelease-artifacts verify
```

## Maven Central

The Maven Central profile expects Sonatype Central Portal credentials under the `central` server ID and a configured GPG key:

```shell
mvn -Pcentral-publishing deploy
```

Configured metadata:

| Metadata | Value |
| --- | --- |
| License | Zero-Clause BSD |
| Project URL | `https://github.com/rszabo50/message-bridge` |
| SCM URL | `https://github.com/rszabo50/message-bridge` |
| Developer | `rszabo50` |
| Target repository | Maven Central Portal |

Before the first Central publish, verify namespace ownership for `io.github.rszabo50` in the Central Portal.

## GitHub Actions Secrets

The release workflow expects these repository secrets:

| Secret | Purpose |
| --- | --- |
| `CENTRAL_USERNAME` | Central Portal token username |
| `CENTRAL_PASSWORD` | Central Portal token password |
| `GPG_PRIVATE_KEY` | ASCII-armored private signing key |
| `GPG_PASSPHRASE` | Passphrase for the signing key |

The public GPG key must be available from a public keyserver before Maven Central can verify signatures.

## Release Workflow

The CI workflow runs on pushes and pull requests to `main`.

The release workflow runs manually or when a tag matching `v*` is pushed:

```shell
git tag v0.1.0
git push origin v0.1.0
```

## Not Yet Configured

- Maven Central namespace validation
