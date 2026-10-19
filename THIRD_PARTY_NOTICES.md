# Attribution and Third-Party Notices

This project is a maintained fork of the OpenVox Language plugin for JetBrains IDEs.

## Current Maintained Fork

- Current maintainer: Roman Borodavkin
- Current repository: https://github.com/barad1tos/Puppet-JetBrains
- Current fork and modifications: Copyright 2026 Roman Borodavkin and contributors
- Current fork license: Apache License 2.0

This current-fork notice covers this repository's derivative work and modifications. It does not replace or narrow the original project attribution preserved below.

## Original OpenVox Language Plugin

- Original project: https://github.com/blendbyte/openvox-intellij
- Original author: Blendbyte GmbH
- Original license: Apache License 2.0
- Original copyright notice preserved in `LICENSE` and `NOTICE`: Copyright 2026 Blendbyte GmbH

The current repository preserves the Apache License 2.0 terms and attribution for the original work. The original project's `NOTICE` text is reproduced in full in this repository's `NOTICE`.

## Upstream Sources of the Grammar and Language Data

- The grammar in `src/main/grammar/OpenVox.bnf` and the lexer in `src/main/grammar/OpenVox.flex` were written by the original author from the OpenVox 8 reference implementation (`lib/puppet/pops/parser/lexer2.rb`, `egrammar.ra` and the EPP support files) and the published language specification. OpenVox is maintained by the OpenVox project (Vox Pupuli) under the Apache License 2.0: https://github.com/OpenVoxProject/openvox
- The TSV files under `src/main/resources/openvox` are generated from the OpenVox sources by `tools/generate-openvox-data.py`; the function, type and attribute descriptions they carry originate in the OpenVox codebase.
- `src/main/grammar/idea-flex.skeleton` is the JFlex skeleton from IntelliJ IDEA Community Edition, licensed under the Apache License 2.0: https://github.com/JetBrains/intellij-community
- The Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/`) is Copyright the original authors and licensed under the Apache License 2.0.
- `settings.gradle.kts` follows the structure of the IntelliJ Platform Plugin Template by JetBrains, licensed under the Apache License 2.0: https://github.com/JetBrains/intellij-platform-plugin-template

## Bundled Runtime Dependencies

The plugin distribution bundles no third-party runtime libraries. The IntelliJ Platform and the Kotlin standard library are provided by the target IDE; the Kotlin compiler, Grammar-Kit, detekt, ktlint, Kover and the test libraries are build- or test-scope only and are not shipped in the plugin ZIP.

## Major Changes in This Fork

- Published under the new plugin identity `software.barad1tos.puppet-tools` with the display name Puppet Tools; the Kotlin package moved to `software.barad1tos.puppet`.
- Marked incompatible with the original plugin IDs, because both plugins register the same language and file types.
- Added a tag-driven release workflow, CodeQL, dependency updates, static analysis (detekt, ktlint), coverage reporting and release-metadata verification.
- Replaced the original logo with a neutral icon; the original artwork is not used.

## Trademark Notice

Puppet is a trademark of Perforce Software, Inc. OpenVox is a project of Vox Pupuli. JetBrains, IntelliJ IDEA and related names or marks are trademarks of their respective owners. This project is not affiliated with, endorsed by or sponsored by Perforce, Vox Pupuli, JetBrains or the original maintainer.
