<p align="center">
  <img src="assets/logo.svg" alt="Puppet Tools" width="160">
</p>

<p align="center">
  <strong>Puppet and OpenVox language support for JetBrains IDEs.</strong>
</p>

<p align="center">
  <a href="https://github.com/barad1tos/Puppet-JetBrains/actions/workflows/ci.yml">
    <img src="https://github.com/barad1tos/Puppet-JetBrains/actions/workflows/ci.yml/badge.svg" alt="CI">
  </a>
  <a href="https://codecov.io/gh/barad1tos/Puppet-JetBrains">
    <img src="https://codecov.io/gh/barad1tos/Puppet-JetBrains/graph/badge.svg" alt="Coverage">
  </a>
  <a href="https://github.com/barad1tos/Puppet-JetBrains/releases/latest">
    <img src="https://img.shields.io/github/v/release/barad1tos/Puppet-JetBrains?include_prereleases&label=Release&colorA=1F2430&colorB=FFCC66" alt="Latest release">
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/license-Apache--2.0-23C284?colorA=1F2430" alt="Apache 2.0 license">
  </a>
</p>

<br>

Puppet Tools brings Puppet and OpenVox manifest editing to IntelliJ Platform IDEs: a Grammar-Kit parser for `.pp` manifests and `.epp` templates, completion and quick documentation backed by data generated from the OpenVox sources, navigation and rename across a module or control repository, a formatter that follows the style guide, and inspections with quick fixes. It is for people who maintain real Puppet code and want the editing, navigation, and project ergonomics of IntelliJ IDEA, PhpStorm, RubyMine or GoLand instead of a text editor.

The project is a maintained fork of the Apache-2.0 OpenVox Language plugin by Blendbyte GmbH. The parser, lexer, code insight and bundled language data come from that project; this fork keeps them under a new plugin identity with its own release pipeline, static analysis and verification coverage.

## What you get today

- **Manifests as an IDE language, not plain text** — syntax highlighting with a configurable colour scheme, brace matching, folding, live templates and a structure view for `.pp` and `.epp` files.
- **Navigation for real modules** — go to the declaration of a class, defined type, function, type alias, parameter or variable; find usages; rename; Go to Class and Go to Symbol; jump from `epp()`, `template()` and `source =>` to the file they name; Hiera gutter links for matching keys in `data/*.yaml`.
- **Data-driven completion** — resource attributes and their allowed values, data types and type aliases, resource types, fact keys inside `$facts[...]`, built-in and project functions, and variables in scope, with quick documentation and OpenVox 8/9 compatibility notes.
- **Formatting that follows the style guide** — two-space indentation and configurable alignment of resource, hash and selector arrows, on its own code style page.
- **Inspections with quick fixes** — unknown function, unknown attribute, unresolved variable, unused parameter, and legacy top-scope facts rewritten as `$facts[...]`.
- **Optional external validation** — `puppet parser validate` and `puppet-lint` results inline in the editor when the executables are available.

## Language Support

| Area       | Support                                                                                                                                                                 |
|------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| File types | `.pp` manifests, `.epp` templates                                                                                                                                       |
| Runtimes   | Puppet 8, OpenVox 8, OpenVox 9 prereleases (checked against 9.0.0-rc1); see [COMPATIBILITY.md](COMPATIBILITY.md)                                             |
| Syntax     | Parameterised data types such as `Optional[Variant[String[1], Enum['a','b']]]`, `Sensitive`, `Init`, type aliases, Puppet-language functions, lambdas, heredocs, EPP tags |
| Completion | Attributes and values, data types, resource types, facts, built-in and project functions, variables in scope                                                           |
| Navigation | Declarations, find usages, rename, Go to Class and Go to Symbol, template and file references, Hiera gutter links                                                       |
| Formatting | Indentation and arrow alignment for resource bodies, hashes and selectors                                                                                               |
| Inspections| Unknown function, unknown attribute, unresolved variable, unused parameter, legacy fact with quick fix                                                                  |
| Validation | Optional `puppet parser validate` and `puppet-lint` integration                                                                                                         |

## Compatibility

- Minimum: IntelliJ Platform `252` (JetBrains IDEs 2025.2)
- Maximum: none — `until-build` is open
- Works in every IntelliJ Platform IDE; depends only on `com.intellij.modules.platform`
- JVM target: 21
- Cannot be installed alongside the original OpenVox Language plugin (`net.blendbyte.openvox-intellij`), because both register the same language and file types; uninstall it first

## Installation

### From disk

1. Download the `.zip` from [Releases](https://github.com/barad1tos/Puppet-JetBrains/releases/latest)
2. **Settings** → **Plugins** → **⚙** → **Install Plugin from Disk...**
3. Select the downloaded `.zip` and restart the IDE

A JetBrains Marketplace listing is planned; this README will link it once it exists.

## Usage Notes

`puppet parser validate` and `puppet-lint` run when their executables are found on `PATH` (`puppet`, `openvox` or `puppet-lint`). Configure explicit paths or disable either check under **Settings** → **Tools** → **Puppet**. Point validation at the runtime you deploy with; the editor supports Puppet 8 and OpenVox 8/9 without a language switch, and `parser validate` alone does not check function arguments, module dependencies or catalog behaviour.

Known limitations inherited from the parser:

- Heredoc bodies are highlighted, but references inside them are not parsed.
- Hiera links match qualified keys in data files; they do not evaluate `hiera.yaml` hierarchies.
- Puppetfile support and parameter information popups are not implemented.

## Building from source

Requires JDK 21 (the Gradle toolchain resolver downloads one when it is missing).

```bash
./gradlew buildPlugin        # Build the distribution ZIP
./gradlew test               # Run the test suite
./gradlew runIde             # Launch a sandbox IDE with the plugin
./gradlew verifyPlugin       # Verify against the recommended IDE targets
python3 -m unittest discover -s tools -p 'test_*.py'   # Language-data generator tests
```

Output: `build/distributions/puppet-tools-<version>.zip`

To run the parser against an external manifest and template corpus:

```bash
./gradlew test --tests '*CorpusTest*' -Dopenvox.corpus=/path/to/modules:/path/to/control-repo
```

Use your operating system's path separator between corpus roots. Corpus tests are skipped when no roots are supplied.

### Language data

Completion and quick documentation read TSV files under `src/main/resources/openvox`, generated from the OpenVox sources with OpenVox 8 as the baseline and version differences noted in quick documentation. Fact paths are curated by hand. To refresh the generated data:

```bash
git clone --depth 1 --branch 8.x https://github.com/OpenVoxProject/openvox.git /tmp/openvox
python3 tools/generate-openvox-data.py /tmp/openvox
```

Grammar sources live in `src/main/grammar`, the Kotlin implementation in `src/main/kotlin`, and generated lexer and parser code stays under `build/`.

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines and [CHANGELOG.md](CHANGELOG.md) for release history.

## Community

- Found a bug? [Open a bug report](https://github.com/barad1tos/Puppet-JetBrains/issues/new?template=bug_report.yml)
- Have a feature idea? [Open a feature request](https://github.com/barad1tos/Puppet-JetBrains/issues/new?template=feature_request.yml)
- Security issue? Use [GitHub Private Vulnerability Reporting](https://github.com/barad1tos/Puppet-JetBrains/security/advisories/new)

## Relationship to the Original Project

This project is derived from the [OpenVox Language](https://github.com/blendbyte/openvox-intellij) plugin by Blendbyte GmbH, distributed under the Apache License 2.0. The grammar, lexer, code insight, inspections and bundled language data remain based on that work. This maintained fork keeps the Apache-2.0 attribution in `LICENSE`, `NOTICE` and `THIRD_PARTY_NOTICES.md` while publishing the plugin under a new identity, `software.barad1tos.puppet-tools`, with its own release pipeline.

The current maintained fork and modifications are Copyright 2026 Roman Borodavkin and contributors.

## Credits

- Maintained fork, release pipeline and verification coverage by Roman Borodavkin and contributors
- Original OpenVox Language plugin by Blendbyte GmbH, distributed under the Apache License 2.0
- Grammar and lexer written from the OpenVox reference implementation by the OpenVox project (Vox Pupuli)
- JFlex skeleton from IntelliJ IDEA Community Edition and the IntelliJ Platform SDK by JetBrains

## License

[Apache 2.0](LICENSE) — see `LICENSE`, `NOTICE`, and `THIRD_PARTY_NOTICES.md`.

Puppet is a trademark of Perforce Software, Inc. OpenVox is a project of Vox Pupuli. This plugin is an independent, community-maintained project and is not affiliated with, endorsed by or sponsored by Perforce, Vox Pupuli or Blendbyte GmbH.
