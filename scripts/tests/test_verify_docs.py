#!/usr/bin/env python3
"""Behavioural tests for the release docs metadata checks in scripts/verify_docs."""

from __future__ import annotations

import sys
import tempfile
import textwrap
import unittest
from pathlib import Path
from unittest import mock

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from verify_docs import changelog, gradle_properties, plugin_xml  # noqa: E402
from verify_docs.release_metadata import check_release_metadata  # noqa: E402
from verify_docs.report import Report  # noqa: E402
from verify_docs.visible_traces import check_visible_traces  # noqa: E402

CHANGELOG = textwrap.dedent(
    """\
    # Changelog

    ## [0.3.0] - 2026-10-19

    - [Change] Published as Puppet Tools (`software.barad1tos.puppet-tools`).
    - [Fix] Settings pages are grouped under Puppet.

    ## [0.2.1] - 2026-09-14

    - Upstream release.
    """,
)

CHANGE_NOTES = """
      <h3>0.3.0</h3>
      <ul>
        <li>[Change] Published as Puppet Tools (<code>software.barad1tos.puppet-tools</code>).</li>
        <li>[Fix] Settings pages are grouped under Puppet.</li>
      </ul>
"""


def plugin_xml_text(change_notes: str = CHANGE_NOTES, descriptor: str = "", description: str = "Puppet Tools.") -> str:
    return (
        "<idea-plugin>\n"
        f"  {descriptor}\n"
        f"  <description><![CDATA[{description}]]></description>\n"
        f"  <change-notes><![CDATA[{change_notes}]]></change-notes>\n"
        "</idea-plugin>\n"
    )


class VerifyDocsTest(unittest.TestCase):
    def setUp(self) -> None:
        self.directory = Path(tempfile.mkdtemp(prefix="verify-docs-"))
        self.addCleanup(lambda: [path.unlink() for path in self.directory.iterdir()] and self.directory.rmdir())

    def run_checks(self, *, version: str = "0.3.0", changelog_text: str = CHANGELOG, xml_text: str | None = None) -> Report:
        gradle = self.directory / "gradle.properties"
        gradle.write_text(f"pluginGroup = software.barad1tos\npluginVersion = {version}\n", encoding="utf-8")
        changelog_path = self.directory / "CHANGELOG.md"
        changelog_path.write_text(changelog_text, encoding="utf-8")
        xml_path = self.directory / "plugin.xml"
        xml_path.write_text(plugin_xml_text() if xml_text is None else xml_text, encoding="utf-8")
        report = Report()
        with (
            mock.patch.object(gradle_properties, "GRADLE_PROPERTIES", gradle),
            mock.patch.object(changelog, "CHANGELOG", changelog_path),
            mock.patch.object(plugin_xml, "PLUGIN_XML", xml_path),
        ):
            check_release_metadata(report)
            check_visible_traces(report)
        return report

    def messages(self, report: Report) -> list[str]:
        return [f"{finding.source}: {finding.message}" for finding in report.findings]

    def test_consistent_release_passes(self) -> None:
        report = self.run_checks()
        self.assertEqual([], self.messages(report))

    def test_version_mismatch_is_reported(self) -> None:
        report = self.run_checks(version="0.4.0")
        self.assertTrue(any("does not match pluginVersion 0.4.0" in message for message in self.messages(report)))

    def test_changelog_bullet_missing_from_change_notes(self) -> None:
        notes = CHANGE_NOTES.replace("        <li>[Fix] Settings pages are grouped under Puppet.</li>\n", "")
        report = self.run_checks(xml_text=plugin_xml_text(change_notes=notes))
        self.assertIn("plugin.xml: CHANGELOG bullet missing from change-notes: [Fix] Settings pages are grouped under Puppet.", self.messages(report))

    def test_change_note_bullet_missing_from_changelog(self) -> None:
        notes = CHANGE_NOTES.replace("      </ul>", "        <li>[New] Only in Marketplace notes.</li>\n      </ul>")
        report = self.run_checks(xml_text=plugin_xml_text(change_notes=notes))
        self.assertIn("CHANGELOG.md: plugin.xml change-note bullet missing from CHANGELOG: [New] Only in Marketplace notes.", self.messages(report))

    def test_free_plugin_without_product_descriptor_has_no_descriptor_errors(self) -> None:
        report = self.run_checks()
        self.assertFalse(any("release-" in message for message in self.messages(report)))

    def test_paid_descriptor_is_checked_against_changelog(self) -> None:
        descriptor = '<product-descriptor code="PPUPPET" release-date="20260101" release-version="03"/>'
        report = self.run_checks(xml_text=plugin_xml_text(descriptor=descriptor))
        self.assertTrue(any("release-date 20260101 does not match" in message for message in self.messages(report)))

    def test_malformed_plugin_xml_is_reported_not_raised(self) -> None:
        report = self.run_checks(xml_text="<idea-plugin><broken")
        self.assertTrue(report.has_errors)
        self.assertTrue(all("not well-formed XML" in message for message in self.messages(report)))

    def test_tool_traces_in_visible_text_are_reported(self) -> None:
        report = self.run_checks(xml_text=plugin_xml_text(description="Written with an AI assistant."))
        self.assertIn("plugin.xml description: contains forbidden visible trace: assistant", self.messages(report))


if __name__ == "__main__":
    unittest.main()
