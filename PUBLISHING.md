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

The release workflow keeps `pom.xml` on a snapshot version in source control. On tag builds, it strips the leading `v` from the tag and runs:

```shell
mvn --batch-mode versions:set -DnewVersion=<tag-version> -DgenerateBackupPoms=false
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

The release workflow runs only when a release tag is pushed. Accepted tag patterns:

- `v1.2.3`
- `v1.2.3-rc.1`
- `v1.2.3-beta.2`

```shell
git tag v0.1.0
git push origin v0.1.0
```

## Not Yet Configured

- Maven Central namespace validation
