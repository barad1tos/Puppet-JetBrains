# Security Policy

## Scope

Puppet Tools is a JetBrains IDE language plugin. Its relevant security surface includes:

- The optional external annotator, which writes the edited manifest to a temporary file and runs the configured `puppet`, `openvox` or `puppet-lint` executable on it.
- Parsing of manifests, EPP templates and Hiera YAML files from the open project.
- IntelliJ Platform plugin descriptor and extension wiring.
- Gradle dependencies and plugin distribution integrity.

The plugin should not run anything other than the configured validators, should not claim official affiliation, and should not process private data beyond normal IDE/plugin operation.

## Supported Versions

| Version | Supported |
|---------|-----------|
| 0.3.x   | Yes       |
| < 0.3   | No        |

Only the latest release line receives security fixes.

## Reporting a Vulnerability

Preferred: use GitHub Private Vulnerability Reporting from this repository's Security tab:

https://github.com/barad1tos/Puppet-JetBrains/security/advisories/new

Please include:

- Description of the vulnerability.
- Steps to reproduce.
- Affected versions.
- Potential impact.
- Any relevant sample `.pp`, `.epp` or Hiera data file.

## What to Expect

- Acknowledgment within 7 days.
- Assessment and response within 30 days.
- For confirmed issues: a fix in the next practical release.
- Credit in the changelog unless you prefer anonymity.

## What Qualifies

- Execution of anything other than the user-configured validators, or validator invocation with unexpected arguments or input.
- Unsafe handling of the temporary validation file or of project files.
- Dependency vulnerabilities in the Gradle build or distribution chain.
- Plugin signing, packaging, or distribution-integrity issues.

## What Does Not Qualify

- A validator doing what the user configured it to do.
- Parser false positives, missing completion, or ordinary IDE UI bugs.
- JetBrains IDE vulnerabilities; report those to JetBrains.
- Theoretical attacks requiring physical access to the developer's machine.

## Bug Bounty

This is a solo-maintained project. There is no monetary bug bounty, but confirmed reporters can be credited in release notes.
