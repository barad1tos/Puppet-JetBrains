"""Release docs metadata checks, split into per-concern modules.

Entry point remains `scripts/verify-docs.py`. The public surface of this package is [cli.main]; every
other module is an implementation detail that the CLI orchestrator imports.

Module map:
  paths             - REPO_ROOT and file path constants
  report            - Finding / Report types
  gradle_properties - gradle.properties parser (pluginVersion)
  changelog         - CHANGELOG.md release entry parser
  plugin_xml        - plugin.xml description, change-notes and optional product-descriptor
  normalization     - text normalization for CHANGELOG vs change-notes comparison
  release_metadata  - version, date and change-note consistency checks
  visible_traces    - forbidden tool traces in user-visible release text
  cli               - argparse + orchestration
"""
