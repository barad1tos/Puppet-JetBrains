# Contributing to Puppet Tools

Thanks for your interest in contributing. This project maintains Puppet and OpenVox language support for JetBrains IDEs, so useful contributions usually fall into parser coverage, completion and quick documentation data, inspections, formatting, tests, or release documentation.

By participating, you agree to follow the [Code of Conduct](.github/CODE_OF_CONDUCT.md).

## Getting Started

1. Fork the repository and clone it locally.
2. Install **JDK 21**, or let the Gradle toolchain resolver download one.
3. Build the plugin:
   ```bash
   ./gradlew buildPlugin
   ```
4. Run tests:
   ```bash
   ./gradlew test --stacktrace
   python3 -m unittest discover -s tools -p 'test_*.py'
   ```
5. Verify plugin compatibility when touching the descriptor, platform APIs or dependencies:
   ```bash
   ./gradlew verifyPlugin --stacktrace
   ```

## Project Structure

| Path                                         | What it does                                                                                        |
|----------------------------------------------|-----------------------------------------------------------------------------------------------------|
| `src/main/grammar/OpenVox.bnf`               | Grammar-Kit grammar source for manifests and EPP templates                                          |
| `src/main/grammar/OpenVox.flex`              | JFlex lexer source                                                                                  |
| `src/main/kotlin/software/barad1tos/puppet/` | Hand-written Kotlin plugin code; class names keep the upstream `OpenVox` prefix                     |
| `src/main/resources/META-INF/plugin.xml`     | Plugin descriptor, extension registrations and Marketplace description                              |
| `src/main/resources/openvox/*.tsv`           | Generated language data: functions, types, attributes, metaparameters, data types and curated facts |
| `tools/generate-openvox-data.py`             | Generator that rebuilds the TSV files from an OpenVox checkout                                      |
| `src/test/kotlin/`                           | Parser, lexer, completion, resolve, inspection, formatter and regression tests                      |
| `scripts/`                                   | Release-metadata and plugin-verifier checks used by CI                                              |
| `build.gradle.kts`                           | Gradle build, Grammar-Kit generation, ktlint, detekt, Kover and plugin-verifier configuration       |

## Generated Code

The lexer and parser are generated into `build/generated/sources/grammar` during the build; nothing generated is committed. Change `OpenVox.bnf` or `OpenVox.flex` and rebuild. Grammar changes are high-impact because they affect PSI, completion, references, inspections and formatting, so include parser fixtures or regression tests for every grammar behaviour change.

The TSV language data is regenerated, not edited by hand, except for `facts.tsv`, which is curated:

```bash
git clone --depth 1 --branch 8.x https://github.com/OpenVoxProject/openvox.git /tmp/openvox
python3 tools/generate-openvox-data.py /tmp/openvox
```

Keep OpenVox 8 as the generated-data baseline so 8-only signatures stay documented; see [COMPATIBILITY.md](COMPATIBILITY.md).

## Submitting Changes

1. Create a focused branch, for example `fix/heredoc-folding` or `docs/compatibility-9-0`.
2. Keep one PR to one logical change.
3. Use Conventional Commits-style messages (`feat:`, `fix:`, `docs:`, `test:`, `chore:`, `ci:`).
4. Include tests for parser, resolver, completion, inspection or formatter changes.
5. Include screenshots only when a user-visible IDE UI changes.

## Good PR Checklist

- `./gradlew build --stacktrace` passes.
- `./gradlew test --stacktrace` and the generator tests pass, or the exact environment limitation is documented.
- `./gradlew detekt ktlintCheck` reports no new findings; the baselines cover the inherited code only.
- `./gradlew verifyPlugin --stacktrace` passes for descriptor, platform API or dependency changes.
- `python3 scripts/verify-docs.py` passes when `gradle.properties`, `CHANGELOG.md` or `plugin.xml` change.
- Marketplace-facing docs avoid unsupported affiliation, compatibility or feature claims.

## License

By contributing, you agree that your contributions are licensed under the [Apache License 2.0](LICENSE).
