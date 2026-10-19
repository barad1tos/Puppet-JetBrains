# Changelog

All notable changes to Puppet Tools will be documented in this file.

## [0.3.0] - 2026-10-19

- [Change] The plugin is now published as Puppet Tools (`software.barad1tos.puppet-tools`) by Roman Borodavkin, a maintained fork of the OpenVox Language plugin by Blendbyte GmbH. The original plugin IDs are marked incompatible because both plugins register the same language and file types; uninstall OpenVox Language before installing Puppet Tools.
- [Change] Settings, code style, colour scheme and inspection pages are grouped under Puppet instead of OpenVox / Puppet; the `.pp` file type is described as a Puppet manifest.
- [Change] New neutral plugin and file icons replace the original logo.
- [Change] Releases are built, verified and published from GitHub Actions on `v*` tags, with CodeQL, dependency updates, static analysis and coverage reporting on every change.

## [0.2.1] - 2026-09-14

- Last release of the upstream [OpenVox Language](https://github.com/blendbyte/openvox-intellij) plugin by Blendbyte GmbH, the base of this fork. Its history is preserved in this repository.
